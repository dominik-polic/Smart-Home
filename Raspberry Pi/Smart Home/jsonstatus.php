<?php
$servername = "localhost";
$username = "USERNAME";
$password = "PASSWORD";
$dbname = "data_db";
$requested_node_name=$_GET["node"];
$requested_node_value=$_GET["action"];
// Create connection
$conn = mysqli_connect($servername, $username, $password, $dbname);
// Check connection
if (!$conn) {
    die("ERROR1");
}


	$first= 1;
	$sql = "SELECT node_name, node_state_current FROM iot_nodes";
	$result = mysqli_query($conn, $sql);
	echo "{\n";
	if (mysqli_num_rows($result) > 0) {
		// output data of each row
		while($row = mysqli_fetch_assoc($result)) {
			if($first== 1){
				$first=0;
			}else{
				echo ",\n";
			}
			echo "\"" . $row["node_name"]. "\": \"" . $row["node_state_current"]."\"";
		}
	} else {
		echo "ERROR2";
	}

	echo "\n}";
mysqli_close($conn);
?>

