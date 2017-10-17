<?php
$servername = "localhost";
$username = "USERNAME";
$password = "PASSWORD";
$dbname = "data_db";
$requested_node_name=$_GET["node"];
$requested_node_value=$_GET["action"];
$requested_node_user=$_GET["user"];
// Create connection
$conn = mysqli_connect($servername, $username, $password, $dbname);
// Check connectinn
if (!$conn) {
    die("ERROR1");
}


if($requested_node_value=="read"){
	$sql = "SELECT node_state_current FROM iot_nodes WHERE node_name='".$requested_node_name."'";
	$result = mysqli_query($conn, $sql);
	if (mysqli_num_rows($result) > 0) {

		while($row = mysqli_fetch_array($result)) {
			echo "" . $row["node_state_current"];
		}
	} else {
		echo "ERROR2";
	}
}else{		
	$sql = "UPDATE iot_nodes SET node_state_desired='".$requested_node_value."' WHERE node_name='".$requested_node_name."'";

	if ($conn->query($sql) == TRUE) {
		echo "Success";
	} else {
		echo "ERROR3";
	}
	
	
}

mysqli_close($conn);


$cod='./writeit.sh '.$requested_node_name.':'.$requested_node_value.' '.$requested_node_user.' 2>&1';
chdir('/var/www/html');
$outputt = shell_exec($cod);
echo $outputt;


?>


