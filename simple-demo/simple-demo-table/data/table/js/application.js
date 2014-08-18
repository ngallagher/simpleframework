var templates = {};
var records = {};
var schemas = {};
var connections = 0;
var attempts = 0;
var total = 1;

function connect() {
	var user = extractParameter("user");
	var type = extractParameter("type");	
	var company = extractParameter("company");
	var products = extractParameter("products");
	var companies = extractParameter("companies");		
	var socket = new WebSocket("ws://localhost:6060/" + type + "?user=" + user + "&company=" + company + "&products=" + products + "&companies=" + companies);

	socket.onopen = function() {
		attempts = 1;
		connections++;
		reportStatus(this, "success.png", "0", "0", "0", "0", "0", "");
	};

	socket.onerror = function(message) {
		reportStatus(this, "failure.png", "0", "0", "0", "0", "0", "");
	};

	socket.onclose = function(message) {
		var exponent = Math.pow(2, attempts++);
		var interval = (exponent - 1) * 1000;
		var reference = connect();

		if (interval > 30 * 1000) {
			interval = 30 * 1000;
		}
		setTimeout(reference, interval);
		reportStatus(this, "pending.png", "0", "0", "0", "0", "0", "");
	};

	socket.onmessage = function(message) {
		var data = message.data.substring(1);

		if (message.data.charAt(0) == 'T') {
			deltaUpdate(this, data);			
		} else if (message.data.charAt(0) == 'S') {
			schemaUpdate(this, data);
		}
	};
}

function openDialog(address, name, width, height) {	
	var handle = window.open(address, name, 'height=' + height + ',width=' + width);
		
	if(handle != undefined) {
		if (handle.focus) {
			handle.focus()
		}
	}
	return false;	
}

function reportStatus(socket, status, height, delta, change, duration, sequence, address) {
	var user = extractParameter("user");
	var image = '<img src="img/';
	
	image += status;
	image += '"';
	image += 'style="';
	image += ' max-width: 100%;';
	image += ' max-height: 25px;';
	image += ' padding-top: 4px;';
    image += ' padding-bottom: 4px;';
	image += ' padding-left: 4px;';
    image += ' padding-right: 8px;';    
	image += '"/>';
	
    document.getElementById("connection").innerHTML = image;
    document.getElementById("rows").innerHTML = height;
    document.getElementById("changes").innerHTML = change;
    document.getElementById("duration").innerHTML = duration;  
	socket.send("status:rows="+height+",change="+change+",duration="+duration+",sequence="+sequence+",address="+address+",user="+user);
	
}

function schemaUpdate(socket, message) {
	var parts = message.split('|');
	var address = parts[0];
	var table = w2ui[address];
	
	if(table != null) {
		var minimum = parts.length - 1;
		var width = table.columns.length;	
		
		if(width == 0) {
			schemas[table.name] = [];
			templates[table.name] = [];
			records[table.name] = [];
		}
		var schema = schemas[table.name];
		
		for ( var i = 1; i < parts.length; i++) {
			var part = parts[i];
			var values = part.split(',');
			var name = values[0];
			var caption = decodeValue(values[1]);
			var template = decodeValue(values[2]);
			var style = decodeValue(values[3]);		
			var resizable = values[4];
			var sortable = values[5];
			var hidden = values[6];		
			var column = {};
			
			column['name'] = name;
			column['caption'] = caption;
			column['style'] = style;		
			column['template'] = template;
			column['resizable'] = resizable == "true";
			column['sortable'] = sortable == "true";
			column['hidden'] = hidden == "true";
			column['token'] = "{" + name + "}";			
			column['pattern'] = new RegExp("{" + name + "}", "g");
			
			schema[i - 1] = column;
		}
		if(width < minimum) {
			expandWidth(table);
			requestRefresh(socket, 'schemaUpdate');
		}	
	}
}

function requestRefresh(socket, message) {
	socket.send('refresh:everything=true,message='+message);
}

function extractParameter(name) {
	var source = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var expression = "[\\?&]"+source+"=([^&#]*)";
	var regex = new RegExp(expression);
	var results = regex.exec(window.location.href);
	
	if( results == null ) {
		return "";
	}  
	return results[1];
}

function deltaUpdate(socket, message) {	
	var header = message.indexOf(':');
	var sequence = 0;
	
	if(header > 0) {
		sequence = message.substring(0, header);
		message = message.substring(header + 1);
	}
	var parts = message.split('|');
	var address = parts[0];
	var table = w2ui[address];
	var schema = schemas[address];
	
	if(table != null) {
		var length = message.length;
		var start = currentTime();
		var total = 0;
		
		if(schema.length > 0) {
			total += updateTable(socket, table, parts);
		}
		var finish = currentTime();
		var duration = finish - start;
		var height = table.total;	
	
		reportStatus(socket, "success.png", height, length, total, duration, sequence, address);
	}
}

function currentTime() {
	var date = new Date()
	return date.getTime();
}

function findRow(table, row) {
	var record = table.find({recid: row});
	var height = table.total;
	var index = 0;
	
	if(record.length > 0) {
		index = record[0];
	} else {
		index = height + 1;	
	}
	return index;
}

function updateTable(socket, table, rows) {
	var changes = [];
	var require = 0;
	var count = 0;
	var total = 0;
	
	for ( var i = 1; i < rows.length; i++) {
		var update = rows[i];
		var pair = update.split(':');	
		var row = parseInt(pair[0]);
		var change = {index: row, delta: pair[1]};
		
		if(require < change.index) {
			require = change.index;
		}
		changes[count++] = change;
	}
	var height = table.total;
	
	if (height <= require) {
		expandHeight(table, require);
	}
	for ( var i = 0; i < changes.length; i++) {
		var index = changes[i].index;
		var delta = changes[i].delta;
		var row = findRow(table, index);
		var cells = delta.split(',');

		if (cells.length > 0) {
			updateRow(socket, table, row, cells);
		}
		total += cells.length;
	}
	return total;
}

function updateRow(socket, table, row, cells) {
	var record = records[table.name][row];
	var template = templates[table.name][row];	
	var schema = schemas[table.name];	
	
	for ( var i = 0; i < cells.length; i++) {
		var cell = cells[i].split('=');		
		var column = cell[0];
		var value = cell[1];
		var style = schema[column];
		var decoded = decodeValue(value);
		
		record[style.name] = decoded;
	}
	interpolateRow(table, record, template);
	table.set(record.recid, template, false);
	reconcileRow(socket, table, row);
}

function interpolateRow(table, record, template) {
	var schema = schemas[table.name];
	
	for ( var i = 0; i < schema.length; i++) {
		var column = schema[i];
		var style = column.style;
		var name = column.name;
		var text = column.template;			
		
		template.style[i] = interpolateCell(table, record, style, 2);
		template[name] = interpolateCell(table, record, text, 2);
	}
}

function interpolateCell(table, record, text, recurse) {
	var schema = schemas[table.name];
	
	for( var j = 0; j < schema.length; j++) {
		var index = text.indexOf('{');
		
		if(index == -1) {
			break;
		}
		var pattern = schema[j].pattern;
		var token = schema[j].token;
		var key = schema[j].name;
		var value = record[key];
		
		if(recurse > 0) {
			index = value.indexOf('{');
			
			if(index != -1) {
				value = interpolateCell(table, record, value, recurse - 1);
			}
		}
		var match = text.indexOf(token); // quicker check
		
		if(match != -1) {
			text = text.replace(pattern, value);
		}
	}
	return text;
}

function reconcileRow(socket, table, row) {
	var template = templates[table.name][row];
	var schema = schemas[table.name];
	var index = findRow(table, row);
	var row = table.get(index);
	
	for( var i = 0; i < schema.length; i++) {
		var style = schema[i];
		var name = style.name;
		var actual = row[name];
		var expect = template[name];
		
		if(actual != expect) {
			requestRefresh(socket, 'reconcileFailure');
		}		
	}	
}

function decodeValue(value) {
	var text = value.substring(1);

	if (value.charAt(0) == '<') {
		var encoded = text.toString();
		var decoded = '';

		for ( var i = 0; i < encoded.length; i += 2) {
			var next = encoded.substr(i, 2);
			var decimal = parseInt(next, 16);

			decoded += String.fromCharCode(decimal);
		}
		return decoded;
	}
	return text;
}

function expandWidth(table) {
	var schema = schemas[table.name];	
	var width = table.columns.length;	
	var height = table.total;
	
	for ( var i = width; i < schema.length; i++) {
		var style = schema[i];	
		var column = {};
		
		column['field'] = style.name;
		column['caption'] = style.caption;
		column['resizable'] = style.resizable;
		column['sortable'] = style.sortable;
		column['hidden'] = style.hidden;
		column['size'] = '60px';		

		for( var j = 0; j < height; j++) {
			templates[table.name][i][name] = '';
			records[table.name][i][name] = '';
		}
		table.addColumn(column);
	}
}

function expandHeight(table, row) {
	var schema = schemas[table.name];	
	var height = table.total;
	var additions = [];
	var count = 0;
	
	for ( var i = height; i <= row; i++) {
		var record = {recid : i, style: []};
		var template = {recid : i, style: []};		
		
		for( var j = 0; j < schema.length; j++) {
			var name = schema[j].name;
			
			template[name] = '';
			record[name] = '';
		}
		additions[count++] = template;
		templates[table.name][i] = template;
		records[table.name][i] = record;
	}
	if(count > 0) {
    	table.add(additions);	
	}
}

window.addEventListener("load", connect, false);