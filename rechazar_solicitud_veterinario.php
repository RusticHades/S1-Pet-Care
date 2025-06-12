<?php
header('Content-Type: application/json');
require_once 'conexion.php';

$response = ['success' => false, 'message' => ''];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $solicitud_id = $_POST['solicitud_id'] ?? '';
    
    try {
        $stmt = $pdo->prepare("UPDATE solicitudes_veterinarios 
                              SET estado = 'rechazado' 
                              WHERE id = ?");
        $stmt->execute([$solicitud_id]);
        
        $response['success'] = true;
        $response['message'] = 'Solicitud rechazada correctamente';
    } catch (PDOException $e) {
        $response['message'] = 'Error al rechazar solicitud: ' . $e->getMessage();
    }
} else {
    $response['message'] = 'Método no permitido';
}

echo json_encode($response);
?>