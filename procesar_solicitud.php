<?php
header('Content-Type: application/json');

// Datos de conexión directamente en el archivo
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

// Crear conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    die(json_encode(['success' => false, 'message' => 'Error de conexión: ' . $conn->connect_error]));
}

// Obtener datos del POST
$data = json_decode(file_get_contents('php://input'), true);
$solicitud_id = $data['solicitud_id'] ?? null;
$accion = $data['accion'] ?? null; // 'aprobar' o 'rechazar'

if (!$solicitud_id || !$accion) {
    echo json_encode(['success' => false, 'message' => 'Datos incompletos']);
    exit;
}

// Iniciar transacción
$conn->begin_transaction();

try {
    // Obtener información de la solicitud
    $stmt = $conn->prepare("SELECT usuario_id FROM solicitudes_veterinarios WHERE id = ?");
    $stmt->bind_param("i", $solicitud_id);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        throw new Exception("Solicitud no encontrada");
    }
    
    $solicitud = $result->fetch_assoc();
    $usuario_id = $solicitud['usuario_id'];
    
    if ($accion === 'aprobar') {
        // Actualizar estado de la solicitud
        $stmt = $conn->prepare("UPDATE solicitudes_veterinarios SET estado = 'aprobado' WHERE id = ?");
        $stmt->bind_param("i", $solicitud_id);
        $stmt->execute();
        
        // Actualizar tipo de usuario a veterinario
        $stmt = $conn->prepare("UPDATE usuarios SET tipo_usuario = 'veterinario', es_veterinario = 1, verificado = 1 WHERE id = ?");
        $stmt->bind_param("i", $usuario_id);
        $stmt->execute();
        
        $message = "Solicitud aprobada con éxito. El usuario ahora es veterinario.";
    } else {
        // Actualizar estado de la solicitud a rechazado
        $stmt = $conn->prepare("UPDATE solicitudes_veterinarios SET estado = 'rechazado' WHERE id = ?");
        $stmt->bind_param("i", $solicitud_id);
        $stmt->execute();
        
        $message = "Solicitud rechazada. El usuario puede intentar nuevamente más tarde.";
    }
    
    $conn->commit();
    echo json_encode(['success' => true, 'message' => $message]);
    
} catch (Exception $e) {
    $conn->rollback();
    echo json_encode(['success' => false, 'message' => 'Error: ' . $e->getMessage()]);
}

$conn->close();
?>