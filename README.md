# ⚙️ ReciclaUPAO - Backend Module

API REST en Spring Boot para gestión de actividades de reciclaje con integración blockchain.

**🌐 Red Actual:** Sepolia Testnet  
**📍 Smart Contract:** `0x6Ee68256eF29096e8Bc66c14494E5f58650488DD`  
**🔍 Etherscan:** https://sepolia.etherscan.io/address/0x6Ee68256eF29096e8Bc66c14494E5f58650488DD

---

## 📋 Prerequisitos

- **Java JDK:** 21 (configurado en el proyecto)
- **Maven:** 3.6+ (o usar wrapper incluido)
- **MySQL:** 8.0 o superior
- **Blockchain Node:**
  - **Localhost:** Hardhat Node corriendo
  - **Sepolia:** No requiere nodo local (usa RPC público/Alchemy)

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

### Blockchain - Localhost (Desarrollo)

```properties
blockchain.enabled=true
blockchain.network=localhost
blockchain.rpc-url=http://127.0.0.1:8545
blockchain.chain-id=31337
blockchain.token-address=0x5FbDB2315678afecb367f032d93F642f64180aa3
blockchain.backend-private-key=0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d
```

### Blockchain - Sepolia (Testnet)

```properties
blockchain.enabled=true
blockchain.network=sepolia
blockchain.rpc-url=https://eth-sepolia.g.alchemy.com/v2/VQ_jKkFIWE-kn56xsm1Is
blockchain.chain-id=11155111
blockchain.token-address=0x6Ee68256eF29096e8Bc66c14494E5f58650488DD
blockchain.backend-private-key=21ffa3d3721e3d86c87a9db030fea21a6e815c545814b0719e45acf33e1e586a
```

> ⚠️ **Para producción:** Usar variables de entorno en lugar de hardcodear private keys

### IPFS (Pinata)

```properties
ipfs.pinata.api-key=5efd595edd6b58314aa5
ipfs.pinata.secret-key=870cc731d19d38efab65d9f248c677b42792a47f0f3544109089175a5d64c26f
```

### Wallets de Validadores (ONGs)

#### Localhost (Hardhat)
```properties
validator1.wallet=0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC
validator1.private-key=0x5de4111afa1a4b94908f83103eb1f1706367c2e68ca870fc3fb9a804cdab365a

validator2.wallet=0x90F79bf6EB2c4f870365E785982E1f101E93b906
validator2.private-key=0x7c852118294e51e653712a81e05800f419141751be58f605c371e15141b007a6
```

#### Sepolia (Testnet)
```properties
validator1.wallet=0x7386e0F040439A743e51e156A20C88792763cBCd
validator1.private-key=21ffa3d3721e3d86c87a9db030fea21a6e815c545814b0719e45acf33e1e586a

validator2.wallet=0x7386e0F040439A743e51e156A20C88792763cBCd
validator2.private-key=21ffa3d3721e3d86c87a9db030fea21a6e815c545814b0719e45acf33e1e586a
```

> 📝 Para producción en Sepolia: crear wallets separadas por rol con SepoliaETH

---

## 🚀 Ejecución

### Opción 1: Maven Wrapper (Recomendado)

```bash
# Windows
.\mvnw clean install
.\mvnw spring-boot:run

# Linux/Mac
./mvnw clean install
./mvnw spring-boot:run
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

### ❌ Error: "Blockchain connection failed" (Localhost)

**Causa:** Hardhat node no está corriendo.

**Solución:**
```bash
cd ../recicla-upao-token
npx hardhat node
```

### ❌ Error: "Blockchain connection timeout" (Sepolia)

**Causa:** RPC URL incorrecta o sin conexión a internet.

**Solución:**
1. Verifica tu RPC URL en `application.properties`
2. Prueba con Alchemy: https://www.alchemy.com/
3. Verifica conexión a internet

### ❌ Error: "No se crearon usuarios"

**Solución:**

Borrar BD y reiniciar backend:
```bash
mysql -u root -p -e "DROP DATABASE recicla_db; CREATE DATABASE recicla_db;"
.\mvnw spring-boot:run
```

### ❌ Usuarios con wallets incorrectas

**Localhost:**
Verifica que las wallets en `application.properties` sean las de Hardhat:
- `admin.wallet` → `0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266`
- `validator1.wallet` → `0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC`
- `validator2.wallet` → `0x90F79bf6EB2c4f870365E785982E1f101E93b906`

**Sepolia:**
Asegúrate de tener SepoliaETH en las wallets configuradas.

### ❌ Error: "Tuple10 cannot be converted to DynamicStruct"

**Causa:** Version incompatible del contrato generado.

**Solución:**
```bash
# Regenerar contrato wrapper
cd ../recicla-upao-token
npx hardhat compile
cd ../recicla_upao_nube
.\mvnw clean install
```

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

# Generar contratos wrapper Web3j
.\mvnw web3j:generate-sources
```

---

## 📚 Tecnologías

- **Spring Boot:** 3.1.5
- **Java:** 21
- **Spring Security:** JWT Authentication
- **Spring Data JPA:** Acceso a datos
- **MySQL:** 8.0+ Base de datos relacional
- **Lombok:** Reducir boilerplate
- **Web3j:** 4.10.3 Integración con blockchain Ethereum
- **JavaMail:** Envío de emails
- **Validation:** Jakarta Bean Validation
- **IPFS/Pinata:** Almacenamiento descentralizado de evidencias

---

## 🌐 Configuración de Red

### Localhost (Desarrollo)

```properties
blockchain.network=localhost
blockchain.rpc-url=http://127.0.0.1:8545
blockchain.chain-id=31337
blockchain.token-address=0x5FbDB2315678afecb367f032d93F642f64180aa3
```

**Requisitos:**
- Hardhat node corriendo
- Contrato desplegado

### Sepolia (Testnet)

```properties
blockchain.network=sepolia
blockchain.rpc-url=https://eth-sepolia.g.alchemy.com/v2/API_KEY
blockchain.chain-id=11155111
blockchain.token-address=0x6Ee68256eF29096e8Bc66c14494E5f58650488DD
```

**Requisitos:**
- Cuenta de Alchemy/Infura
- Wallets con SepoliaETH
- Smart contract desplegado en Sepolia

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
6. Rotación de secrets periódica

---

## ✅ Checklist de Configuración

### Localhost (Desarrollo)
- [ ] Java JDK 21 instalado
- [ ] MySQL 8.0+ instalado y corriendo
- [ ] Base de datos `recicla_db` creada
- [ ] Hardhat node corriendo en `http://127.0.0.1:8545`
- [ ] Contrato desplegado en `0x5FbDB...180aa3`
- [ ] `application.properties` configurado para localhost
- [ ] Backend inicia sin errores
- [ ] Usuarios creados automáticamente
- [ ] Endpoint `/actuator/health` responde `{"status":"UP"}`

### Sepolia (Testnet)
- [ ] Wallets con SepoliaETH (mínimo 0.05 ETH)
- [ ] Cuenta de Alchemy/Infura configurada
- [ ] Smart contract desplegado en Sepolia
- [ ] Dirección del contrato actualizada en `application.properties`
- [ ] Private keys de wallets configuradas
- [ ] RPC URL funcionando
- [ ] Backend conecta a Sepolia exitosamente
- [ ] Transacciones de prueba funcionan

---

## 📖 Documentación Relacionada

- **Blockchain:** `../recicla-upao-token/README.md`
- **Setup Completo:** `../recicla-upao-token/SETUP.md`
- **Migración Sepolia:** `../recicla-upao-token/SEPOLIA_SETUP.md`
- **Frontend:** `../recicla_app_front/README.md`

---

## 🎯 Estado Actual del Proyecto

### ✅ Completado
- [x] API REST completa con Spring Boot
- [x] Integración blockchain con Web3j
- [x] Autenticación JWT + Spring Security
- [x] Multi-firma de ONGs (2/2 validaciones)
- [x] Subida de evidencias a IPFS/Pinata
- [x] Sistema de roles (Admin, ONG, Centro, Participante)
- [x] Centro puede registrar actividades para estudiantes
- [x] Historial de actividades por usuario
- [x] Historial de actividades registradas por Centro
- [x] **Funcionando en Localhost (Hardhat Local)**
- [x] **Funcionando en Sepolia Testnet**
- [x] Mint automático de tokens al aprobar 2 ONGs
- [x] Burn de tokens al canjear recompensas

### ⏳ En Desarrollo
- [ ] Deploy en VPS de producción
- [ ] Notificaciones en tiempo real
- [ ] Dashboard de administrador mejorado
- [ ] Reportes y estadísticas avanzadas

### 🔮 Futuras Mejoras
- [ ] Deploy en Ethereum Mainnet
- [ ] Sistema de niveles y gamificación
- [ ] Integración con Polygon/BSC
- [ ] App móvil nativa (React Native)
- [ ] Sistema de referidos y recompensas

---

**Puerto:** 8080  
**Base URL:** http://localhost:8080  
**Health Check:** http://localhost:8080/actuator/health  
**API Docs:** http://localhost:8080/swagger-ui.html

**Autor:** JhoneiroLove  
**Versión:** 1.0.0  
**Última Actualización:** Diciembre 2025  
**Licencia:** MIT
