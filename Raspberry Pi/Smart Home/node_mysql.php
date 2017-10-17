<?php
$servername = "localhost";
$username = "USERNAME";
$password = "PASSWORD";
$dbname = "data_db";
$requested_node_name=$_GET["node"];
$requested_node_action=$_GET["action"];
if($requested_node_action!="write")
	$requested_node_action="read";
$requested_node_row=$_GET["row"];
$requested_node_value=$_GET["value"];
// Create connection
$conn = mysqli_connect($servername, $username, $password, $dbname);
// Check connection
if (!$conn) {
    die("ERROR");
}


if($requested_node_action=="read"){
	$sql = "SELECT ".$requested_node_row." FROM iot_nodes WHERE node_name='".$requested_node_name."'";
	$result = mysqli_query($conn, $sql);
	if (mysqli_num_rows($result) > 0) {
		// output data of each row
		while($row = mysqli_fetch_assoc($result)) {
			echo "OK:" . $row[$requested_node_row];
		}
	} else {
		echo "ERROR";
	}
}else{		
	$sql = "UPDATE iot_nodes SET ".$requested_node_row."='".$requested_node_value."' WHERE node_name='".$requested_node_name."'";

	if ($conn->query($sql) === TRUE) {
		echo "OK.";
	} else {
		echo "ERROR";
	}
	
}

mysqli_close($conn);
?>
