<?php

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

require '../phpMailer/src/Exception.php';
require '../phpMailer/src/PHPMailer.php';
require '../phpMailer/src/SMTP.php';

function sendAccountDeletionEmail($userEmail, $userName) {
    try {
        $mail = new PHPMailer(true);

        $mail->isSMTP();                              
        $mail->Host       = 'smtp.gmail.com';        
        $mail->SMTPAuth   = true;                    
        $mail->Username   = 'jame.lucero.swu@phinmaed.com';   
        $mail->Password   = 'fbbavvnlpjwricdv';      
        $mail->SMTPSecure = 'ssl';                   
        $mail->Port       = 465;

        $mail->setFrom('jame.lucero.swu@phinmaed.com', 'Memorix'); 
        $mail->addAddress($userEmail);            
        $mail->addReplyTo('jame.lucero.swu@phinmaed.com', 'Memorix'); 

        $mail->isHTML(true);                         
        $mail->Subject = 'Account Deletion Notification'; 
        
        $logoUrl = 'https://i.imgur.com/uABwtME.png';

        $bodyContent = '
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f7f7f7; border-radius: 10px; color: #333;">
            <div style="text-align: center; margin-bottom: 0px;">
                <img src="' . $logoUrl . '" alt="Memorix Logo" style="width: 150px; height: 130px; padding: 0;">
            </div>
            <h2 style="text-align: center; color: #d9534f;">Account Deletion Notice</h2>
                <p>Hello ' . htmlspecialchars($userName, ENT_QUOTES, 'UTF-8') . ',</p>
                <p>We are writing to inform you that your account has been <strong>deleted</strong> from our system. This action was taken as per your request or due to our account management policies.</p>
                <div style="background-color: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); margin: 20px 0;">
                    <p style="margin: 0; font-size: 14px;">If you believe this was a mistake, please contact our support team immediately.</p>
                    <p style="margin: 0; font-size: 14px;">Email: <a href="mailto:memorixdev2024@gmail.com" style="color: #337ab7;">memorixdev2024@gmail.com</a></p>
                    <p style="margin: 0; font-size: 14px;">Phone: +639633816671</p>
                </div>
                <p>We appreciate your understanding and are here to assist you with any further queries.</p>
                <p style="text-align: center; margin-top: 30px;">Best regards,<br><strong>Memorix</strong></p>
                <footer style="margin-top: 30px; text-align: center; font-size: 12px; color: #888;">
                    <p>This is an automated message, please do not reply to this email.</p>
                    <p>&copy; ' . date("Y") . ' Memorix. All rights reserved.</p>
                </footer>
            </div>
        ';

        $mail->Body = $bodyContent;

        $mail->send();
        return true;

    } catch (Exception $e) {
        echo "alert('Message could not be sent. Mailer Error: {$mail->ErrorInfo}');</script>";
        return false;
    }
}

if (isset($_POST["deleteAccount"])) {
    $userEmail = filter_input(INPUT_POST, "user-email", FILTER_SANITIZE_EMAIL);
    $userName = filter_input(INPUT_POST, "user-name", FILTER_SANITIZE_SPECIAL_CHARS);

    if (!filter_var($userEmail, FILTER_VALIDATE_EMAIL)) {
        echo "alert('Invalid email address. Please try again.');</script>";
        exit();
    }

    sendAccountDeletionEmail($userEmail, $userName);
}
