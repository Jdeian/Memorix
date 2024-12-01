<?php

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

require '../phpMailer/src/Exception.php';
require '../phpMailer/src/PHPMailer.php';
require '../phpMailer/src/SMTP.php';

function sendSubscriptionStatusEmail($userEmail, $userName, $status, $amount) {
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
        $mail->Subject = 'Subscription Status Notification'; 

        $formattedAmount = number_format($amount, 2);
        $statusColor = $status === 'Accepted' ? '#4CAF50' : '#d9534f'; 
        $statusMessage = $status === 'Accepted' ? "Congratulations! Your subscription has been successfully accepted. We're thrilled to have you on board and look forward to providing you with an exceptional experience." : 'We regret to inform you that, unfortunately, your subscription has been declined. We understand this may be disappointing and encourage you to reach out to our support team for assistance.';

        $logoUrl = 'https://i.imgur.com/uABwtME.png';

        $bodyContent = '
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f7f7f7; border-radius: 10px; color: #333; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);">
                <div style="text-align: center; margin-bottom: 0px;">
                    <img src="' . $logoUrl . '" alt="Memorix Logo" style="width: 150px; height: 120px; padding: 0;">
                </div>
                <h2 style="text-align: center; color: ' . $statusColor . ';">Subscription ' . htmlspecialchars($status, ENT_QUOTES, 'UTF-8') . '</h2>
                <p>Hello ' . htmlspecialchars($userName, ENT_QUOTES, 'UTF-8') . ',</p>
                <p>' . $statusMessage . '</p>

                <div style="background-color: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); margin: 20px 0;">
                    <p style="margin: 0; font-size: 14px;">Amount: <strong>₱' . $formattedAmount . '</strong></p>
                    <p style="margin: 0; font-size: 14px;">Status: <strong>' . htmlspecialchars($status, ENT_QUOTES, 'UTF-8') . '</strong></p>
                </div>

                <div style="background-color: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); margin: 20px 0;">
                <p>If you have any questions, feel free to contact our support team.</p>
                <p style="margin: 0; font-size: 14px;">Email: <a href="mailto:memorixdev2024@gmail.com" style="color: #337ab7;">memorixdev2024@gmail.com</a></p>
                <p style="margin: 0; font-size: 14px;">Phone: +639633816671</p>
                <p style="text-align: center; margin-top: 30px;">Best regards,<br><strong>Memorix</strong></p>
                </div>
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
