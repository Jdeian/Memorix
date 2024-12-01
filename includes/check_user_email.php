<?php
include "../includes/database.php";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = isset($_POST['user-email']) ? $_POST['user-email'] : '';

    $sql = "SELECT COUNT(*) FROM users WHERE email = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->bind_result($count);
    $stmt->fetch();
    
    if ($count > 0) {
        echo json_encode(['exists' => true]);
    } else {
        echo json_encode(['exists' => false]);
    }

    $stmt->close();
}
$conn->close();
?>
