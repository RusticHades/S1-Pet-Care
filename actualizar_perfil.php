<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

// Configuración de la BD
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "miapp_db";

// Respuesta por defecto
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
    $required = ['id', 'usuario', 'correo'];
    foreach ($required as $field) {
        if (empty($data[$field])) {
            throw new Exception("El campo $field es requerido");
        }
    }

    // Asignar datos
    $id = $data['id'];
    $usuario = trim($data['usuario']);
    $correo = trim($data['correo']);
    $telefono = !empty($data['telefono']) && $data['telefono'] !== 'NULL' ? trim($data['telefono']) : null;
    $direccion = !empty($data['direccion']) && $data['direccion'] !== 'NULL' ? trim($data['direccion']) : null;
    $fotoBase64 = $data['foto_usuario'] ?? null;

    // Validaciones adicionales
    if (!filter_var($correo, FILTER_VALIDATE_EMAIL)) {
        throw new Exception("Correo electrónico inválido");
    }

    if ($telefono && !preg_match('/^[0-9]{10,15}$/', $telefono)) {
        throw new Exception("Teléfono debe contener solo números (10-15 dígitos)");
    }

    // Conectar a MySQL
    $conn = new mysqli($servername, $username, $password, $dbname);
    if ($conn->connect_error) {
        throw new Exception("Error de conexión: " . $conn->connect_error);
    }

    // Iniciar transacción
    $conn->begin_transaction();

    try {
        // 1. Actualizar datos básicos
        $query = "UPDATE usuarios SET 
                 usuario = ?, 
                 correo = ?, 
                 telefono = ?, 
                 direccion = ? 
                 WHERE id = ?";
        
        $stmt = $conn->prepare($query);
        $stmt->bind_param("ssssi", $usuario, $correo, $telefono, $direccion, $id);
        
        if (!$stmt->execute()) {
            throw new Exception("Error al actualizar datos: " . $stmt->error);
        }
        $stmt->close();

        // 2. Manejar imagen si se proporciona
        if ($fotoBase64) {
            // Validar Base64
            if (!preg_match('/^[a-zA-Z0-9\/\r\n+]*={0,2}$/', $fotoBase64)) {
                throw new Exception("Formato de imagen inválido");
            }

            // Decodificar imagen
            $imagenDecodificada = base64_decode($fotoBase64);
            if ($imagenDecodificada === false) {
                throw new Exception("Error al decodificar imagen");
            }

            // Validar tamaño (máx 2MB)
            if (strlen($imagenDecodificada) > 2097152) {
                throw new Exception("La imagen es demasiado grande (máx 2MB)");
            }

            // Generar nombre único para la imagen
            $nombreImagen = uniqid("perfil_") . ".jpg";
            $rutaImagen = "images/fotoPerfilUsuarios/" . $nombreImagen;
            $rutaCompleta = $_SERVER['DOCUMENT_ROOT'] . "/miapp/" . $rutaImagen;

            // Crear directorio si no existe
            if (!file_exists(dirname($rutaCompleta))) {
                mkdir(dirname($rutaCompleta), 0777, true);
            }

            // Guardar imagen en el servidor
            if (!file_put_contents($rutaCompleta, $imagenDecodificada)) {
                throw new Exception("Error al guardar la imagen");
            }

            // Actualizar ruta en la BD
            $stmtFoto = $conn->prepare("UPDATE usuarios SET foto_usuario = ? WHERE id = ?");
            $stmtFoto->bind_param("si", $rutaImagen, $id);
            
            if (!$stmtFoto->execute()) {
                throw new Exception("Error al actualizar foto: " . $stmtFoto->error);
            }
            $stmtFoto->close();
        }

        // Confirmar transacción
        $conn->commit();

        $response = [
            'success' => true,
            'message' => 'Perfil actualizado correctamente',
            'usuario' => [
                'id' => $id,
                'usuario' => $usuario,
                'correo' => $correo,
                'telefono' => $telefono,
                'direccion' => $direccion
            ]
        ];

        if (isset($rutaImagen)) {
            $response['foto_url'] = "http://" . $_SERVER['HTTP_HOST'] . "/miapp/" . $rutaImagen;
        }

    } catch (Exception $e) {
        $conn->rollback();
        throw $e;
    }

    $conn->close();

} catch (Exception $e) {
    $response['message'] = $e->getMessage();
}

echo json_encode($response);
?>