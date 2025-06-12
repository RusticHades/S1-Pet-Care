<?php
header('Content-Type: application/json');
// Database connection parameters
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

$response = ['success' => false, 'data' => []];

try {
    // Create connection
    $conn = new PDO("mysql:host=$servername;dbname=$dbname", $username, $password);
    // Set the PDO error mode to exception
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $stmt = $conn->prepare("SELECT s.*, u.usuario, u.correo 
                          FROM solicitudes_veterinarios s
                          JOIN usuarios u ON s.usuario_id = u.id
                          WHERE s.estado = 'pendiente'");
    $stmt->execute();
    
    $solicitudes = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    $response['success'] = true;
    $response['data'] = $solicitudes;
} catch (PDOException $e) {
    $response['message'] = 'Error al obtener solicitudes: ' . $e->getMessage();
}

echo json_encode($response);
?>