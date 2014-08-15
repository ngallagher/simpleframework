<?
require("../../common.php");
$SIDE = array(
   array('Search', '../../search/search.php'),
   array('Feedback', '../../html/links.html')
);
print_template('Simple @core.version@', 'index.inc');
?>
