# ⚙️ ReciclaUPAO - Backend Module

API REST en Spring Boot para gestión de actividades de reciclaje con integración blockchain.

---

## 📋 Prerequisitos

- **Java JDK:** 17 o superior
- **Maven:** 3.6+ (o usar wrapper incluido)
- **MySQL:** 8.0 o superior
- **Hardhat Node:** Debe estar corriendo (ver módulo blockchain)

---

## 🗄️ Configuración de Base de Datos

### 1. Crear Base de Datos

```bash
mysql -u root -p
```

```sql
CREATE DATABASE recicla_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### 2. Verificar

```sql
SHOW DATABASES;
```

> Las tablas se crean automáticamente al iniciar el backend (`spring.jpa.hibernate.ddl-auto=update`)

---

## ⚙️ Configuración (`application.properties`)

### Base de Datos

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/recicla_db
spring.datasource.username=root
spring.datasource.password=root
```

### Blockchain

```properties
blockchain.enabled=true
blockchain.network=localhost
blockchain.rpc-url=http://127.0.0.1:8545
blockchain.token-address=0x5FbDB2315678afecb367f032d93F642f64180aa3
blockchain.backend-private-key=0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d
```

### IPFS (Pinata)

```properties
ipfs.pinata.api-key=5efd595edd6b58314aa5
ipfs.pinata.secret-key=870cc731d19d38efab65d9f248c677b42792a47f0f3544109089175a5d64c26f
```

### Wallets de Usuarios Iniciales

```properties
admin.wallet=0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266
validator1.wallet=0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC
validator2.wallet=0x90F79bf6EB2c4f870365E785982E1f101E93b906
centro.wallet=0x15d34AAf54267DB7D7c367839AAf71A00a2C6A65
```

> 📝 Estas wallets corresponden a las cuentas de Hardhat (deterministas)

---

## 🚀 Ejecución

### Opción 1: Maven Wrapper (Recomendado)

```bash
# Compilar e instalar dependencias
.\mvnw clean install

# Ejecutar aplicación
.\mvnw spring-boot:run
```

### Opción 2: Maven Instalado

```bash
mvn clean install
mvn spring-boot:run
```

### Opción 3: IntelliJ IDEA / Eclipse

1. Importar proyecto Maven
2. Esperar descarga de dependencias
3. Run `ReciclaApplication.java`

---

## ✅ Verificar que Inició Correctamente

### 1. Revisar Logs

Deberías ver:

```
✅ Usuario administrador creado exitosamente
   👤 Username: admin
   🔑 Password: admin123
   💼 Wallet:   0xf39Fd...92266

✅ Validador 1 creado exitosamente
   👤 Username: ong1
   🔑 Password: ong123
   💼 Wallet:   0x3C44C...dD2b48

✅ Validador 2 creado exitosamente
   👤 Username: ong2
   🔑 Password: ong123
   💼 Wallet:   0x90F79...6dB9

✅ Centro de Acopio creado exitosamente
   👤 Username: centroacopio
   🔑 Password: centro123
   💼 Wallet:   0x15d34...2C6A65

Started ReciclaApplication in X.XXX seconds
```

### 2. Probar Endpoint

```bash
# PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"

# Bash/Linux
curl http://localhost:8080/actuator/health
```

**Respuesta esperada:**
```json
{
  "status": "UP"
}
```

---

## 👥 Usuarios Creados Automáticamente

El `DataInitializer` crea estos usuarios al iniciar:

| Username | Password | Rol | Wallet (Hardhat) |
|----------|----------|-----|------------------|
| `admin` | `admin123` | ADMINISTRADOR | Account #0 |
| `ong1` | `ong123` | ONG | Account #2 |
| `ong2` | `ong123` | ONG | Account #3 |
| `centroacopio` | `centro123` | CENTRO_ACOPIO | Account #4 |

---

## 🏗️ Estructura del Proyecto

```
recicla_upao_nube/
├── src/main/java/com/upao/recicla/
│   ├── domain/
│   │   ├── entity/              # Entidades JPA
│   │   └── service/             # Lógica de negocio
│   ├── infra/
│   │   ├── config/              # Configuración (DataInitializer, Security, etc.)
│   │   ├── repository/          # Repositorios JPA
│   │   └── security/            # JWT, filtros, etc.
│   └── web/
│       └── controller/          # Endpoints REST
├── src/main/resources/
│   ├── application.properties   # Configuración principal
│   └── static/                  # Recursos estáticos
└── pom.xml                      # Dependencias Maven
```

---

## 📡 Endpoints Principales

### Autenticación

```
POST /api/auth/login
POST /api/auth/signup
```

### Actividades

```
GET  /actividad/listar
POST /actividad/registro
POST /actividad/registro-centro
GET  /actividad/{id}
```

### Blockchain

```
GET  /blockchain/actividades/pendientes
POST /blockchain/actividades/{id}/aprobar
POST /blockchain/actividades/{id}/rechazar
GET  /blockchain/balance
```

### Recompensas

```
GET  /recompensa/listar
POST /recompensa/registro
POST /recompensa/canjear
```

---

## 🛠️ Troubleshooting

### ❌ Error: "Cannot connect to MySQL"

**Solución:**
```bash
# PowerShell
Get-Service -Name MySQL*
Start-Service -Name MySQL80

# Verificar
mysql -u root -p -e "SHOW DATABASES;"
```

### ❌ Error: "Port 8080 already in use"

**Solución:**

Cambiar puerto en `application.properties`:
```properties
server.port=8081
```

O cerrar proceso:
```bash
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess | Stop-Process
```

### ❌ Error: "Blockchain connection failed"

**Causa:** Hardhat node no está corriendo.

**Solución:**
```bash
cd ../recicla-upao-token
npx hardhat node
```

### ❌ Error: "No se crearon usuarios"

**Solución:**

Borrar BD y reiniciar backend:
```bash
mysql -u root -p -e "DROP DATABASE recicla_db; CREATE DATABASE recicla_db;"
.\mvnw spring-boot:run
```

### ❌ Usuarios con wallets incorrectas

**Causa:** Las wallets en `application.properties` no coinciden con Hardhat.

**Solución:**

Verifica que las wallets en `application.properties` sean:
- `admin.wallet` → Account #0 de Hardhat
- `validator1.wallet` → Account #2
- `validator2.wallet` → Account #3
- `centro.wallet` → Account #4

---

## 🔧 Comandos Útiles

```bash
# Solo compilar
.\mvnw compile

# Compilar sin tests
.\mvnw clean install -DskipTests

# Limpiar target
.\mvnw clean

# Ver dependencias
.\mvnw dependency:tree

# Ejecutar un test específico
.\mvnw test -Dtest=NombreDelTest
```

---

## 📚 Tecnologías

- **Spring Boot:** 3.2.x
- **Spring Security:** JWT Authentication
- **Spring Data JPA:** Acceso a datos
- **MySQL:** Base de datos relacional
- **Lombok:** Reducir boilerplate
- **Web3j:** Integración con blockchain
- **JavaMail:** Envío de emails
- **Validation:** Jakarta Bean Validation

---

## 🔐 Seguridad

> ⚠️ **IMPORTANTE:** Este proyecto usa configuración de **DESARROLLO**

**NO usar en producción:**
- Credenciales MySQL por defecto (`root/root`)
- Private keys en `application.properties`
- API keys de Pinata expuestas
- JWT secret hardcodeado

**Para producción:**
1. Usa variables de entorno
2. Configura Azure Key Vault / AWS Secrets Manager
3. Habilita HTTPS/TLS
4. Implementa rate limiting
5. Configura CORS apropiadamente

---

## ✅ Checklist de Configuración

- [ ] Java JDK 17+ instalado
- [ ] MySQL instalado y corriendo
- [ ] Base de datos `recicla_db` creada
- [ ] Hardhat node corriendo en `http://127.0.0.1:8545`
- [ ] Contrato desplegado en dirección correcta
- [ ] `application.properties` configurado
- [ ] Backend inicia sin errores
- [ ] Usuarios creados automáticamente
- [ ] Endpoint `/actuator/health` responde

---

**Puerto:** 8080  
**Base URL:** http://localhost:8080  
**Health Check:** http://localhost:8080/actuator/health
