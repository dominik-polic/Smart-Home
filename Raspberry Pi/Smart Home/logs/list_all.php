<?php

if($_GET['user']=="USER1"||$_GET['user']=="USER2"||$_GET['user']=="USER3"||$_GET['user']=="USER4"||$_GET['user']=="USER5"||$_GET['user']=="USER6"){
	$files = scandir('.');



		foreach($files as $entry){

			if ($entry != "." && $entry != ".." && $entry != "list_all.php") {
				
				echo $entry;
				echo "\n";				
			}
		}
echo "Success";
	
}else{
	echo "Access denied!";
}

?>