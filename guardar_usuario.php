<?php
header('Content-Type: application/json');

// Configuración de la base de datos
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

// Verificar si es una solicitud POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(["success" => false, "message" => "Método no permitido"]);
    exit;
}

// Verificar campos requeridos
$required_fields = ['usuario', 'correo', 'contrasenia', 'tipo_usuario'];
foreach ($required_fields as $field) {
    if (empty($_POST[$field])) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "El campo $field es requerido"]);
        exit;
    }
}

// Obtener y limpiar datos
$nombre = trim($_POST['usuario']);
$correo = trim($_POST['correo']);
$contrasenia = trim($_POST['contrasenia']);
$tipo_usuario = trim($_POST['tipo_usuario']);
$ruta_imagen = null;

// Validar formato de email
if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "Formato de correo electrónico inválido"]);
    exit;
}

// Validar longitud de contraseña
if (strlen($contrasenia) < 8) {
    http_response_code(400);
    echo json_encode(["success" => false, "message" => "La contraseña debe tener al menos 8 caracteres"]);
    exit;
}

// Hashear la contraseña
$contrasena_hash = password_hash($contrasenia, PASSWORD_DEFAULT);

// Manejar la imagen si fue enviada
if (isset($_FILES['foto_usuario']) && $_FILES['foto_usuario']['error'] === UPLOAD_ERR_OK) {
    $file = $_FILES['foto_usuario'];
    
    // Validar que sea una imagen
    $allowed_types = ['image/jpeg', 'image/png', 'image/gif'];
    $file_info = finfo_open(FILEINFO_MIME_TYPE);
    $mime_type = finfo_file($file_info, $file['tmp_name']);
    finfo_close($file_info);
    
    if (!in_array($mime_type, $allowed_types)) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "Solo se permiten imágenes JPEG, PNG o GIF"]);
        exit;
    }
    
    // Generar nombre único para la imagen
    $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
    $nombre_imagen = uniqid("perfil_") . '.' . $extension;
    $directorio_destino = "C:/xampp/htdocs/miapp/images/fotoPerfilUsuarios/";
    
    // Crear directorio si no existe
    if (!file_exists($directorio_destino)) {
        mkdir($directorio_destino, 0777, true);
    }
    
    $ruta_destino = $directorio_destino . $nombre_imagen;
    
    if (move_uploaded_file($file['tmp_name'], $ruta_destino)) {
        $ruta_imagen = "images/fotoPerfilUsuarios/" . $nombre_imagen;
    } else {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => "Error al guardar la imagen"]);
        exit;
    }
}

// Conectar a la base de datos
try {
    $conn = new mysqli($servername, $username, $password, $dbname);
    
    if ($conn->connect_error) {
        throw new Exception("Error de conexión a la base de datos");
    }
    
    // Verificar si el usuario o correo ya existen
    $stmt_check = $conn->prepare("SELECT id FROM usuarios WHERE usuario = ? OR correo = ?");
    $stmt_check->bind_param("ss", $nombre, $correo);
    $stmt_check->execute();
    $stmt_check->store_result();
    
    if ($stmt_check->num_rows > 0) {
        http_response_code(400);
        echo json_encode(["success" => false, "message" => "El nombre de usuario o correo electrónico ya está en uso"]);
        $stmt_check->close();
        $conn->close();
        exit;
    }
    $stmt_check->close();
    
    // Insertar nuevo usuario
    $stmt = $conn->prepare("INSERT INTO usuarios (usuario, correo, contrasenia, tipo_usuario, foto_usuario) VALUES (?, ?, ?, ?, ?)");
    $stmt->bind_param("sssss", $nombre, $correo, $contrasena_hash, $tipo_usuario, $ruta_imagen);
    
    if ($stmt->execute()) {
        $response = [
            "success" => true,
            "message" => "Usuario registrado correctamente",
            "user_id" => $stmt->insert_id
        ];
        
        if ($ruta_imagen) {
            $response["foto_url"] = "http://192.168.0.192:8080/miapp/" . $ruta_imagen;
        }
        
        echo json_encode($response);
    } else {
        throw new Exception("Error al registrar usuario: " . $stmt->error);
    }
    
    $stmt->close();
    $conn->close();
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
    
    if (isset($conn) && $conn->ping()) {
        $conn->close();
    }
}
?>