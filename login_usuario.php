<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");

// Configuración de la BD
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

// Validar campos
if (empty($_POST['correo']) || empty($_POST['contrasenia'])) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Correo y contraseña son requeridos"
    ]);
    exit;
}

// Datos del POST
$correo = trim($_POST['correo']);
$contrasena = $_POST['contrasenia'];

try {
    $conn = new mysqli($servername, $username, $password, $dbname);
    
    if ($conn->connect_error) {
        throw new Exception("Error de conexión: " . $conn->connect_error);
    }
    
    // Consulta para obtener datos del usuario
    $stmt = $conn->prepare("
        SELECT 
            id, 
            usuario, 
            correo, 
            contrasenia, 
            tipo_usuario, 
            foto_usuario
        FROM usuarios 
        WHERE correo = ?
    ");
    
    if (!$stmt) {
        throw new Exception("Error en la consulta: " . $conn->error);
    }
    
    $stmt->bind_param("s", $correo);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        http_response_code(401);
        echo json_encode([
            "success" => false,
            "message" => "Usuario no encontrado"
        ]);
        exit;
    }
    
    $usuario = $result->fetch_assoc();
    $stmt->close();
    
    // Verificar contraseña
    if (!password_verify($contrasena, $usuario['contrasenia'])) {
        http_response_code(401);
        echo json_encode([
            "success" => false,
            "message" => "Contraseña incorrecta"
        ]);
        exit;
    }
    
    // Obtener URL de la imagen si existe
    $fotoUrl = null;
    if (!empty($usuario['foto_usuario'])) {
        $fotoUrl = "http://".$_SERVER['HTTP_HOST']."/miapp/".$usuario['foto_usuario'];
    }
    
    // Limpiar datos sensibles
    unset($usuario['contrasenia']);
    
    // Respuesta exitosa
    echo json_encode([
        "success" => true,
        "message" => "Inicio de sesión exitoso",
        "usuario" => $usuario,
        "foto_url" => $fotoUrl
    ]);
    
    $conn->close();
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Error del servidor: " . $e->getMessage()
    ]);
}
?>