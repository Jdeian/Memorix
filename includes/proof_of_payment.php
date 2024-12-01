<?php
if (isset($_GET['file'])) {
    $username = "11193618";
    $password = "60-dayfreetrial";
    $filePath = $_GET['file'];

    $url = "https://pink-boar-882869.hostingersite.com/uploads/" . $filePath;

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_USERPWD, "$username:$password");

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

    curl_close($ch);

    if ($httpCode == 200) {
        header("Content-Type: image/jpeg"); 
        echo $response;
        exit; 
    } else {
        header("HTTP/1.1 404 Not Found");
        echo "Image not found.";
        exit;
    }
}
?>
    <style>
        .proof-modal {
            z-index: 2000;
            display: none;
            position: fixed;
            z-index: 500000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
        }

        .proof-modal::-webkit-scrollbar {
            display: none;
        }

        .proof-modal-content {
            margin: 20px auto;
            display: block;
            width: 100%;
            padding-bottom: 50px;
            max-width: 600px;
            max-width: 300px;
        }
        .close {
            position: absolute;
            top: 15px;
            right: 35px;
            color: #f1f1f1;
            font-size: 40px;
            font-weight: bold;
            cursor: pointer;
        }
    </style>

<div id="proofPaymentModal" class="proof-modal">
    <span class="close" onclick="closeModal()">&times;</span>
    <img class="proof-modal-content" id="proofImage">
</div>

<script> 
function showProofOfPayment(element) {
    const proofPath = element.closest('.proof-payment').getAttribute('data-proof-path');
    if (proofPath) {
        const modal = document.getElementById("proofPaymentModal");
        const modalImg = document.getElementById("proofImage");
        const imageUrl = "../includes/proof_of_payment.php?file=" + encodeURIComponent(proofPath); 
        
        console.log(imageUrl); 
        modalImg.src = imageUrl; 
        modal.style.display = "block"; 
    }
}

function closeModal() {
    const modal = document.getElementById("proofPaymentModal");
    modal.style.display = "none";
}

window.onclick = function(event) {
    const modal = document.getElementById("proofPaymentModal");
    if (event.target == modal) {
        modal.style.display = "none";
    }
}
</script>

</body>
</html>
