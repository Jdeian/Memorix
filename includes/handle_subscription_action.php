<?php
error_reporting(E_ALL);
ini_set('display_errors', '1');

include "../includes/database.php";
include "../includes/subscription-notice.php";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $userId = isset($_POST['user_id']) ? intval($_POST['user_id']) : null;
    $pendingId = isset($_POST['pending_id']) ? intval($_POST['pending_id']) : null;
    $action = isset($_POST['action']) ? $_POST['action'] : null;

    if ($action === 'Accepted' || $action === 'Declined') {
        $pendingQuery = "SELECT amount, proof_payment, created_at FROM subcriptions_pending WHERE id = '$pendingId'";
        $pendingResult = mysqli_query($conn, $pendingQuery);

        if (!$pendingResult) {
            die(json_encode(['success' => false, 'message' => 'Query Error: ' . mysqli_error($conn)]));
        }

        $row = mysqli_fetch_assoc($pendingResult);
        if (!$row) {
            die(json_encode(['success' => false, 'message' => 'No pending subscription found.']));
        }

        $amount = $row['amount'];
        $sentDate = $row['created_at'];
        $proof = $row['proof_payment'];

        $historyQuery = "INSERT INTO subcriptions_history (user_id, pending_id, amount, proof_payment, pending_status, created_at) VALUES ('$userId', '$pendingId', $amount, '$proof', '$action', '$sentDate')";
        if (!mysqli_query($conn, $historyQuery)) {
            die(json_encode(['success' => false, 'message' => 'History Insert Error: ' . mysqli_error($conn)]));
        }

        if ($action === 'Accepted') {
            $category = $amount == 999 ? 'Premium' : ($amount == 150 ? 'Basic' : null);
            
            if ($category) {
                if ($amount == 150) {
                    $expDate = date('Y-m-d', strtotime('+30 days'));
                } elseif ($amount == 999) {
                    $expDate = date('Y-m-d', strtotime('+1 year'));
                } else {
                    $expDate = date('Y-m-d', strtotime('now')); 
                }

                $insertSubscriptionQuery = "INSERT INTO subscriptions (user_id, category, expdate, amount, created_at) VALUES ('$userId', '$category', '$expDate', $amount, NOW())";
                if (!mysqli_query($conn, $insertSubscriptionQuery)) {
                    die(json_encode(['success' => false, 'message' => 'Subscription Insert Error: ' . mysqli_error($conn)]));
                }

                $updateUserQuery = "UPDATE users SET category = '$category' WHERE id = '$userId'";
                if (!mysqli_query($conn, $updateUserQuery)) {
                    die(json_encode(['success' => false, 'message' => 'User Update Error: ' . mysqli_error($conn)]));
                }
            }
        }

        $userQuery = "SELECT email, fullname FROM users WHERE id = '$userId'";
        $userResult = mysqli_query($conn, $userQuery);
        if ($userResult && mysqli_num_rows($userResult) > 0) {
            $userRow = mysqli_fetch_assoc($userResult);
            $userEmail = $userRow['email'];
            $userName = $userRow['fullname'];

            sendSubscriptionStatusEmail($userEmail, $userName, $action, $amount);
        }

        $deletePendingQuery = "DELETE FROM subcriptions_pending WHERE id = '$pendingId'";
        if (!mysqli_query($conn, $deletePendingQuery)) {
            die(json_encode(['success' => false, 'message' => 'Pending Delete Error: ' . mysqli_error($conn)]));
        }

        echo json_encode(['success' => true, 'message' => "$action subscription processed successfully."]);
    } elseif ($action === 'Delete') {
        $deletePendingQuery = "DELETE FROM subcriptions_history WHERE id = '$pendingId'";
        if (!mysqli_query($conn, $deletePendingQuery)) {
            die(json_encode(['success' => false, 'message' => 'Pending Delete Error: ' . mysqli_error($conn)]));
        }
        
        echo json_encode(['success' => true, 'message' => 'Payment history deleted.']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Invalid action.']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid request method.']);
}
?>
