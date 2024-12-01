<?php

$servernamedb = "localhost";
$usernamedb = "u843230181_Memorix";
$passworddb = "Memorix24";
$dbname = "u843230181_Memorix";

$conn = new mysqli($servernamedb, $usernamedb, $passworddb, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

?>