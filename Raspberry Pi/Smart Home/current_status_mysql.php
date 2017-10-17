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



	$sql = "SELECT node_name, node_state_current FROM iot_nodes";
	$result = mysqli_query($conn, $sql);
	echo "<pre style=\"word-wrap: break-word; white-space: pre-wrap;\">-START-\n";
	if (mysqli_num_rows($result) > 0) {
		// output data of each row
		while($row = mysqli_fetch_assoc($result)) {
				echo $row["node_name"]. ":" . $row["node_state_current"]."\n";

		}
	} else {
		echo "ERROR2";
	}

echo "</pre>";
mysqli_close($conn);
?>

