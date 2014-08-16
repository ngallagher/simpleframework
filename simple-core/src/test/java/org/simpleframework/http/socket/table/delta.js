var templates = new Array();
var records = new Array();
var schema = new Array();
var connections = 0;
var attempts = 0;
var total = 1;

function connect() {
	socket = new WebSocket("ws://localhost:6060/update");

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
		var table = w2ui['mainGrid'];

		if (message.data.charAt(0) == 'T') {
			deltaUpdate(this, table, data, updateTable);
		} else if (message.data.charAt(0) == 'H') {
			deltaUpdate(this, table, data, highlightTable);				
		} else if (message.data.charAt(0) == 'S') {
			schemaUpdate(this, table, data);
		}
	};
}

function reportStatus(socket, status, height, delta, change, duration, sequence, method) {
	var image = '<img src="';
	
	image += status;
	image += '"';
	image += 'style="';
	image += ' max-width: 100%;';
	image += ' max-height: 100%;';
	image += ' padding-top: 4px;';
    image += ' padding-bottom: 4px;';
	image += ' padding-left: 4px;';
    image += ' padding-right: 8px;';    
	image += '"/>';
	
    document.getElementById("connection").innerHTML = image;
    document.getElementById("rows").innerHTML = height;
    document.getElementById("changes").innerHTML = change;
    document.getElementById("duration").innerHTML = duration;  
	socket.send("status:rows="+height+",change="+change+",duration="+duration+",sequence="+sequence+",method="+method);
	
}

function schemaUpdate(socket, table, message) {
	var cells = message.split('|');
	var minimum = cells.length;
	var width = schema.length;
	
	for ( var i = 0; i < cells.length; i++) {
		var values = cells[i].split(',');
		var name = values[0];
		var caption = decodeValue(values[1]);
		var template = decodeValue(values[2]);
		var resizable = values[3];
		var sortable = values[4];
		var style = {};
		
		style['name'] = name;
		style['caption'] = caption;
		style['template'] = template;
		style['resizable'] = resizable;
		style['sortable'] = sortable;
		
		schema[i] = style;
	}
	if(width < minimum) {
		expandWidth(table);
		requestRefresh(socket, 'schemaUpdate');
	}	
}

function requestRefresh(socket, message) {
	socket.send('refresh:everything=true,message='+message);
}

function deltaUpdate(socket, table, message, method) {	
	var header = message.indexOf(':');
	var sequence = 0;
	
	if(header > 0) {
		sequence = message.substring(0, header);
		message = message.substring(header + 1);
	}
	var rows = message.split('|');
	var length = message.length;
	var start = currentTime();
	
	if(schema.length > 0) {
		method(socket, table, rows);
	}
	var finish = currentTime();
	var duration = finish - start;
	var height = table.total;
	var change = rows.length;
	var operation = method.name;

	reportStatus(socket, "success.png", height, length, change, duration, sequence, operation);
}

function currentTime() {
	var date = new Date()
	return date.getTime();
}

function findRow(table, row) {
	var record = table.find({ recid: row });
	var height = table.total;
	var index = 0;
	
	if(record.length > 0) {
		index = record[0];
	} else {
		index = height + 1;	
	}
	return index;
}

function highlightTable(socket, table, rows) {
	for ( var i = 0; i < rows.length; i++) {
		var row = rows[i];
		var pair = row.split(':');
		var index = pair[0];

		if (index > 0) {
			index = findRow(table, index);
			
			if (pair != null && pair.length > 1) {
				var cells = pair[1].split(',');

				if (cells.length > 0) {
					highlightRow(table, index, cells);
				}
			}
		}
	}
}

function updateTable(socket, table, rows) {
	for ( var i = 0; i < rows.length; i++) {
		var row = rows[i];
		var pair = row.split(':');
		var index = pair[0];

		if (index > 0) {
			index = findRow(table, index);
			
			if (pair != null && pair.length > 1) {
				var cells = pair[1].split(',');

				if (cells.length > 0) {
					updateRow(socket, table, index, cells);
				}
			}
		}
	}
}

function findCell(table, row, column) {
	var height = table.total;
	var width = schema.length;
	
	if(row <= height && column <= width) {
		var expression = "#mainGrid_";
		
		expression += table.name;
		expression += "_rec_";
		expression += row;
		expression += " td[col=";
		expression += column;
		expression += "]";
	
		return $(expression)[0];
	}
	return null;
}

function highlightRow(table, row, cells) {
	var height = table.total;

	if (height <= row) {
		expandHeight(table, row);
	}
	var record = records[row];
	
	for ( var i = 0; i < cells.length; i++) {
		var cell = cells[i].split('=');		
		var column = cell[0];
		var value = cell[1];
		var style = schema[column];
		var decoded = decodeValue(value);
		
		record.style[column] = decoded;
	}
}

function updateRow(socket, table, row, cells) {
	var height = table.total;

	if (height <= row) {
		expandHeight(table, row);
	}
	var record = records[row];
	var template = templates[row];	
	
	for ( var i = 0; i < cells.length; i++) {
		var cell = cells[i].split('=');		
		var column = cell[0];
		var value = cell[1];
		var style = schema[column];
		var decoded = decodeValue(value);
		
		record[style.name] = decoded;
	}
	interpolateRow(record, template);
	table.set(record.recid, template, false);
	reconcileRow(socket, table, row);
}

function interpolateRow(record, template) {
	for ( var i = 0; i < schema.length; i++) {
		var style = schema[i];
		var name = style.name;
		var text = style.template;			
		
		for( var j = 0; j < schema.length; j++) {
			var index = text.indexOf('{');
			
			if(index == -1) {
				break;
			}
			var key = schema[j].name;
			var token = "{" + key + "}";
			var value = record[key];
			
			text = text.replace(token, value);
		}
		template.style[i] = record.style[i];
		template[name] = text;
	}
}

function reconcileRow(socket, table, row) {
	var template = templates[row];
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
			var char = encoded.substr(i, 2);
			var decimal = parseInt(char, 16);

			decoded += String.fromCharCode(decimal);
		}
		return decoded;
	}
	return text;
}

function expandWidth(table) {	
	var width = table.columns.length;
	var height = table.total;
	
	for ( var i = width; i < schema.length; i++) {
		var style = schema[i];	
		var column = {};
		
		column['field'] = style.name;
		column['caption'] = style.caption;
		column['resizable'] = style.resizable;
		column['sortable'] = style.sortable;
		column['size'] = '50px';		

		for( var j = 0; j < height; j++) {
			templates[i][name] = '';
			records[i][name] = '';
		}
		table.addColumn(column);
	}
}

function expandHeight(table, row) {
	var height = table.total;
	
	for ( var i = height; i < row; i++) {
		var index = i + 1;
		var record = {recid : index, id: index, style: []};
		var template = {recid : index, id: index, style: []};		
		
		for( var j = 0; j < schema.length; j++) {
			var name = schema[j].name;
			
			template[name] = '';
			record[name] = '';
		}
		templates[row] = template;
		records[row] = record;
        table.add(template);
	}
}

window.addEventListener("load", connect, false);