<?php
header('Content-Type: application/json');

// Configuración de la base de datos
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

// Configurar charset
$conn->set_charset("utf8");

// Inicializar respuesta
$response = ['success' => false, 'message' => ''];

try {
    // Obtener datos del POST
    $data = json_decode(file_get_contents('php://input'), true);
    
    if ($_SERVER['REQUEST_METHOD'] == 'POST') {
        // Validar campos obligatorios
        $requiredFields = ['id_mascota', 'id_veterinario', 'fecha_cita', 'motivo'];
        foreach ($requiredFields as $field) {
            if (empty($data[$field])) {
                throw new Exception("El campo $field es obligatorio");
            }
        }

        // Sanitizar datos
        $id_mascota = intval($data['id_mascota']);
        $id_veterinario = intval($data['id_veterinario']);
        $fecha_cita = $conn->real_escape_string($data['fecha_cita']);
        $motivo = $conn->real_escape_string($data['motivo']);
        $notas = isset($data['notas']) ? $conn->real_escape_string($data['notas']) : '';
        
        // Validar que la mascota exista
        $sql_check = "SELECT id_mascota FROM mascotas WHERE id_mascota = ?";
        $stmt_check = $conn->prepare($sql_check);
        $stmt_check->bind_param("i", $id_mascota);
        $stmt_check->execute();
        $stmt_check->store_result();
        
        if ($stmt_check->num_rows == 0) {
            throw new Exception("La mascota con ID $id_mascota no existe");
        }
        $stmt_check->close();

        // Insertar la cita
        $sql = "INSERT INTO citas (id_mascota, id_veterinario, fecha_cita, motivo, notas, estado) 
                VALUES (?, ?, ?, ?, ?, 'pendiente')";
        
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("iisss", $id_mascota, $id_veterinario, $fecha_cita, $motivo, $notas);
        
        if ($stmt->execute()) {
            $response['success'] = true;
            $response['message'] = 'Cita agregada correctamente';
            $response['id_cita'] = $stmt->insert_id;
        } else {
            throw new Exception("Error al agregar cita: " . $conn->error);
        }
        
        $stmt->close();
    } else {
        throw new Exception("Método no permitido");
    }
} catch (Exception $e) {
    $response['message'] = $e->getMessage();
} finally {
    $conn->close();
    echo json_encode($response);
}
?>