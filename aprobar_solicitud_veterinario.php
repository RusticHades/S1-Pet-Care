<?php
header('Content-Type: application/json');
require_once 'conexion.php';

$response = ['success' => false, 'message' => ''];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $solicitud_id = $_POST['solicitud_id'] ?? '';
    
    try {
        // Iniciar transacción
        $pdo->beginTransaction();
        
        // 1. Obtener datos de la solicitud
        $stmt = $pdo->prepare("SELECT * FROM solicitudes_veterinarios WHERE id = ?");
        $stmt->execute([$solicitud_id]);
        $solicitud = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if (!$solicitud) {
            throw new Exception('Solicitud no encontrada');
        }
        
        // 2. Actualizar usuario a veterinario
        $stmt = $pdo->prepare("UPDATE usuarios 
                              SET es_veterinario = TRUE, verificado = TRUE, 
                                  telefono = ?, direccion = ?
                              WHERE id = ?");
        $stmt->execute([
            $solicitud['telefono'],
            $solicitud['ubicacion'],
            $solicitud['usuario_id']
        ]);
        
        // 3. Actualizar estado de la solicitud
        $stmt = $pdo->prepare("UPDATE solicitudes_veterinarios 
                              SET estado = 'aprobado' 
                              WHERE id = ?");
        $stmt->execute([$solicitud_id]);
        
        $pdo->commit();
        
        $response['success'] = true;
        $response['message'] = 'Solicitud aprobada correctamente';
    } catch (Exception $e) {
        $pdo->rollBack();
        $response['message'] = 'Error al aprobar solicitud: ' . $e->getMessage();
    }
} else {
    $response['message'] = 'Método no permitido';
}

echo json_encode($response);
?>