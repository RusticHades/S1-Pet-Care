<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

$response = ['success' => false, 'message' => 'Error desconocido'];

try {
    // Verificar método POST
    if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
        throw new Exception("Método no permitido");
    }

    // Obtener datos JSON
    $json = file_get_contents('php://input');
    $data = json_decode($json, true);
    
    if (json_last_error() !== JSON_ERROR_NONE) {
        throw new Exception("Error en formato JSON: " . json_last_error_msg());
    }

    // Validar campos requeridos
    $required = ['id_usuario', 'nombre', 'especie', 'sexo'];
    foreach ($required as $field) {
        if (empty($data[$field])) {
            throw new Exception("El campo $field es requerido");
        }
    }

    // Configuración de la BD
    $servername = "localhost";
    $username = "root";
    $password = "";
    $dbname = "miapp_db";

    // Conectar a MySQL
    $conn = new mysqli($servername, $username, $password, $dbname);
    if ($conn->connect_error) {
        throw new Exception("Error de conexión: " . $conn->connect_error);
    }

    // Preparar datos
    $id_usuario = $data['id_usuario'];
    $nombre = $conn->real_escape_string($data['nombre']);
    $especie = $conn->real_escape_string($data['especie']);
    $raza = isset($data['raza']) ? $conn->real_escape_string($data['raza']) : null;
    $fecha_nacimiento = isset($data['fecha_nacimiento']) ? $data['fecha_nacimiento'] : null;
    $edad = isset($data['edad']) ? (int)$data['edad'] : null;
    $sexo = $conn->real_escape_string($data['sexo']);
    $peso = isset($data['peso']) ? (float)$data['peso'] : null;
    $esterilizado = isset($data['esterilizado']) ? (bool)$data['esterilizado'] : false;
    $foto_mascota = isset($data['foto_mascota']) ? $data['foto_mascota'] : null;

    // Insertar mascota
    $query = "INSERT INTO mascotas (
        id_usuario, nombre, especie, raza, fecha_nacimiento, 
        edad, sexo, peso, esterilizado, foto_mascota
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    $stmt = $conn->prepare($query);
    $stmt->bind_param(
        "issssisibs",
        $id_usuario, $nombre, $especie, $raza, $fecha_nacimiento,
        $edad, $sexo, $peso, $esterilizado, $foto_mascota
    );
    
    if ($stmt->execute()) {
        $response = [
            'success' => true,
            'message' => 'Mascota registrada exitosamente',
            'id_mascota' => $stmt->insert_id
        ];
    } else {
        throw new Exception("Error al registrar mascota: " . $stmt->error);
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    $response['message'] = $e->getMessage();
}

echo json_encode($response);
?>