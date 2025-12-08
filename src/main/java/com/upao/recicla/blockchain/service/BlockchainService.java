package com.upao.recicla.blockchain.service;

import com.upao.recicla.blockchain.contracts.ReciclaToken;
import com.upao.recicla.blockchain.dto.ActividadPropuesta;
import com.upao.recicla.blockchain.dto.BlockchainBalance;
import com.upao.recicla.blockchain.dto.TransactionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class BlockchainService {

    private final Web3j web3j;
    private final Credentials backendCredentials;
    private final StaticGasProvider gasProvider;
    private final String tokenContractAddress;

    public TransactionResult registerUserOnChain(String walletAddress, String dni) {
        try {
            log.info("📝 Registrando usuario en blockchain: wallet={}", walletAddress);

            String dniHash = Hash.sha3String(dni);

            ReciclaToken contract = ReciclaToken.load(
                    tokenContractAddress,
                    web3j,
                    backendCredentials,
                    gasProvider);

            TransactionReceipt receipt = contract.addToWhitelist(walletAddress, dniHash).send();

            log.info("✅ Usuario registrado. TX: {}", receipt.getTransactionHash());

            return TransactionResult.success(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue());

        } catch (Exception e) {
            log.error("❌ Error registrando usuario", e);
            return TransactionResult.failure(e.getMessage());
        }
    }

    /**
     * Propone una actividad de reciclaje en blockchain (nuevo sistema de
     * multi-firma)
     * El backend solo PROPONE, no acuña directamente. Requiere aprobación de
     * validadores.
     *
     * @param userWallet    Wallet del usuario que recicló
     * @param pesoKg        Peso del material en kilogramos
     * @param tipoMaterial  Tipo de material (plastico, papel, vidrio, metal,
     *                      carton, organico)
     * @param evidenciaIPFS Hash IPFS de la evidencia fotográfica
     * @return Resultado de la transacción con ID de la actividad propuesta
     */
    public TransactionResult proponerActividad(String userWallet, Integer pesoKg, String tipoMaterial,
            String evidenciaIPFS) {
        try {
            log.info("📝 Proponiendo actividad: usuario={}, material={}, peso={}kg, evidencia={}",
                    userWallet, tipoMaterial, pesoKg, evidenciaIPFS);

            ReciclaToken contract = ReciclaToken.load(
                    tokenContractAddress,
                    web3j,
                    backendCredentials,
                    gasProvider);

            // Llamar a proponerActividad en el contrato
            TransactionReceipt receipt = contract.proponerActividad(
                    userWallet,
                    BigInteger.valueOf(pesoKg),
                    tipoMaterial,
                    evidenciaIPFS).send();

            log.info("✅ Actividad propuesta. TX: {} - Esperando aprobación de validadores (0/2)",
                    receipt.getTransactionHash());

            return TransactionResult.success(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue());

        } catch (Exception e) {
            log.error("❌ Error proponiendo actividad", e);
            return TransactionResult.failure(e.getMessage());
        }
    }

    /**
     * Validador aprueba una actividad propuesta
     * Cuando se alcanzan 2 aprobaciones, el contrato minta tokens automáticamente
     *
     * @param actividadId          ID de la actividad a aprobar
     * @param validadorCredentials Credenciales del validador (no del backend)
     * @return Resultado de la transacción
     */
    public TransactionResult aprobarActividad(BigInteger actividadId, Credentials validadorCredentials) {
        try {
            log.info("✅ Validador aprobando actividad #{}", actividadId);

            ReciclaToken contract = ReciclaToken.load(
                    tokenContractAddress,
                    web3j,
                    validadorCredentials, // Importante: usar credenciales del validador
                    gasProvider);

            TransactionReceipt receipt = contract.aprobarActividad(actividadId).send();

            log.info("✅ Actividad aprobada. TX: {}", receipt.getTransactionHash());

            return TransactionResult.success(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue());

        } catch (Exception e) {
            log.error("❌ Error aprobando actividad", e);
            return TransactionResult.failure(e.getMessage());
        }
    }

    /**
     * Validador rechaza una actividad propuesta
     *
     * @param actividadId          ID de la actividad a rechazar
     * @param razon                Razón del rechazo
     * @param validadorCredentials Credenciales del validador
     * @return Resultado de la transacción
     */
    public TransactionResult rechazarActividad(BigInteger actividadId, String razon, Credentials validadorCredentials) {
        try {
            log.info("❌ Validador rechazando actividad #{}: {}", actividadId, razon);

            ReciclaToken contract = ReciclaToken.load(
                    tokenContractAddress,
                    web3j,
                    validadorCredentials,
                    gasProvider);

            TransactionReceipt receipt = contract.rechazarActividad(actividadId, razon).send();

            log.info("✅ Actividad rechazada. TX: {}", receipt.getTransactionHash());

            return TransactionResult.success(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue());

        } catch (Exception e) {
            log.error("❌ Error rechazando actividad", e);
            return TransactionResult.failure(e.getMessage());
        }
    }

    /**
     * Consulta información de una actividad propuesta
     *
     * @param actividadId ID de la actividad
     * @return Información de la actividad
     */
    public ActividadPropuesta getActividad(BigInteger actividadId) {
        try {
            ReciclaToken contract = ReciclaToken.load(
                    tokenContractAddress,
                    web3j,
                    backendCredentials,
                    gasProvider);

            org.web3j.abi.datatypes.DynamicStruct actividad = contract.getActividad(actividadId).send();

            @SuppressWarnings("rawtypes")
            java.util.List components = actividad.getValue();
            return ActividadPropuesta.builder()
                    .actividadId(actividadId.longValue())
                    .usuarioWallet(((org.web3j.abi.datatypes.Type) components.get(1)).getValue().toString())
                    .pesoKg(((BigInteger) ((org.web3j.abi.datatypes.Type) components.get(2)).getValue()).intValue())
                    .tipoMaterial(((org.web3j.abi.datatypes.Type) components.get(3)).getValue().toString())
                    .evidenciaIPFS(((org.web3j.abi.datatypes.Type) components.get(4)).getValue().toString())
                    .tokensCalculados(
                            Convert.fromWei(((org.web3j.abi.datatypes.Type) components.get(5)).getValue().toString(),
                                    Convert.Unit.ETHER))
                    .aprobaciones(
                            ((BigInteger) ((org.web3j.abi.datatypes.Type) components.get(7)).getValue()).intValue())
                    .ejecutada((Boolean) ((org.web3j.abi.datatypes.Type) components.get(8)).getValue())
                    .rechazada((Boolean) ((org.web3j.abi.datatypes.Type) components.get(9)).getValue())
                    .build();

        } catch (Exception e) {
            log.error("❌ Error consultando actividad", e);
            return null;
        }
    }

    public TransactionResult burnTokensForRedemption(String userWallet, BigDecimal tokenAmount, String description) {
        try {
            log.info("🔥 Quemando {} REC de {}", tokenAmount, userWallet);

            BigInteger amountInWei = Convert.toWei(tokenAmount, Convert.Unit.ETHER).toBigInteger();

            ReciclaToken contract = ReciclaToken.load(tokenContractAddress, web3j, backendCredentials, gasProvider);

            TransactionReceipt receipt = contract.burnForRedemption(userWallet, amountInWei, description).send();

            log.info("✅ Tokens quemados. TX: {}", receipt.getTransactionHash());

            return TransactionResult.success(receipt.getTransactionHash(), receipt.getBlockNumber().longValue());

        } catch (Exception e) {
            log.error("❌ Error quemando tokens", e);
            return TransactionResult.failure(e.getMessage());
        }
    }

    public BlockchainBalance getUserBalance(String walletAddress) {
        try {
            ReciclaToken contract = ReciclaToken.load(tokenContractAddress, web3j, backendCredentials, gasProvider);

            BigInteger balance = contract.balanceOf(walletAddress).send();
            BigInteger earned = contract.totalTokensEarnedByUser(walletAddress).send();
            BigInteger spent = contract.totalTokensSpentByUser(walletAddress).send();
            boolean isWhitelisted = contract.isWhitelisted(walletAddress).send();

            return BlockchainBalance.builder()
                    .currentBalance(Convert.fromWei(balance.toString(), Convert.Unit.ETHER))
                    .totalEarned(Convert.fromWei(earned.toString(), Convert.Unit.ETHER))
                    .totalSpent(Convert.fromWei(spent.toString(), Convert.Unit.ETHER))
                    .isWhitelisted(isWhitelisted)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error consultando balance", e);
            return null;
        }
    }

    /**
     * Obtiene todas las actividades pendientes (no ejecutadas ni rechazadas)
     */
    public List<ActividadPropuesta> getPropuestasPendientes() {
        List<ActividadPropuesta> propuestasPendientes = new ArrayList<>();

        try {
            // Obtener el contador total de actividades llamando directamente al contrato
            BigInteger totalActividades = getActividadCounter();

            log.info("📊 Total de actividades en blockchain: {}", totalActividades);

            // Iterar sobre todas las actividades y filtrar las pendientes
            for (BigInteger i = BigInteger.ZERO; i.compareTo(totalActividades) < 0; i = i.add(BigInteger.ONE)) {
                try {
                    ActividadPropuesta propuesta = getActividadDirecta(i);

                    // Solo incluir actividades pendientes (no ejecutadas ni rechazadas)
                    if (propuesta != null && !propuesta.getEjecutada() && !propuesta.getRechazada()) {
                        propuestasPendientes.add(propuesta);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error procesando actividad {}: {}", i, e.getMessage());
                }
            }

            log.info("✅ Propuestas pendientes encontradas: {}", propuestasPendientes.size());

        } catch (Exception e) {
            log.error("❌ Error obteniendo propuestas pendientes", e);
        }

        return propuestasPendientes;
    }

    /**
     * Obtiene una actividad llamando directamente al contrato sin wrapper
     */
    private ActividadPropuesta getActividadDirecta(BigInteger actividadId) throws Exception {
        // Crear la función para llamar a actividades(uint256)
        Function function = new Function(
                "actividades",
                Collections.singletonList(new org.web3j.abi.datatypes.generated.Uint256(actividadId)),
                Arrays.asList(
                        new TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {
                        }, // id
                        new TypeReference<org.web3j.abi.datatypes.Address>() {
                        }, // usuario
                        new TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {
                        }, // pesoKg
                        new TypeReference<org.web3j.abi.datatypes.Utf8String>() {
                        }, // tipoMaterial
                        new TypeReference<org.web3j.abi.datatypes.Utf8String>() {
                        }, // evidenciaIPFS
                        new TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {
                        }, // tokensCalculados
                        new TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {
                        }, // timestamp
                        new TypeReference<org.web3j.abi.datatypes.generated.Uint8>() {
                        }, // aprobaciones
                        new TypeReference<org.web3j.abi.datatypes.Bool>() {
                        }, // ejecutada
                        new TypeReference<org.web3j.abi.datatypes.Bool>() {
                        }, // rechazada
                        new TypeReference<org.web3j.abi.datatypes.Address>() {
                        } // propuestoPor
                ));

        String encodedFunction = FunctionEncoder.encode(function);

        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(
                        backendCredentials.getAddress(),
                        tokenContractAddress,
                        encodedFunction),
                DefaultBlockParameterName.LATEST).send();

        List<Type> result = FunctionReturnDecoder.decode(
                response.getValue(),
                function.getOutputParameters());

        if (result.isEmpty() || result.size() < 11) {
            return null;
        }

        // Extraer valores del resultado
        String usuarioWallet = result.get(1).getValue().toString();
        BigInteger pesoKg = (BigInteger) result.get(2).getValue();
        String tipoMaterial = result.get(3).getValue().toString();
        String evidenciaIPFS = result.get(4).getValue().toString();
        BigInteger tokensCalculadosWei = (BigInteger) result.get(5).getValue();
        BigInteger aprobaciones = (BigInteger) result.get(7).getValue();
        Boolean ejecutada = (Boolean) result.get(8).getValue();
        Boolean rechazada = (Boolean) result.get(9).getValue();

        return ActividadPropuesta.builder()
                .actividadId(actividadId.longValue())
                .usuarioWallet(usuarioWallet)
                .pesoKg(pesoKg.intValue())
                .tipoMaterial(tipoMaterial)
                .evidenciaIPFS(evidenciaIPFS)
                .tokensCalculados(Convert.fromWei(tokensCalculadosWei.toString(), Convert.Unit.ETHER))
                .aprobaciones(aprobaciones.intValue())
                .ejecutada(ejecutada)
                .rechazada(rechazada)
                .build();
    }

    /**
     * Obtiene el contador de actividades directamente del contrato
     */
    private BigInteger getActividadCounter() throws Exception {
        // Crear la función para llamar a actividadCounter()
        Function function = new Function(
                "actividadCounter",
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<org.web3j.abi.datatypes.generated.Uint256>() {
                }));

        String encodedFunction = FunctionEncoder.encode(function);

        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(
                        backendCredentials.getAddress(),
                        tokenContractAddress,
                        encodedFunction),
                DefaultBlockParameterName.LATEST).send();

        List<Type> result = FunctionReturnDecoder.decode(
                response.getValue(),
                function.getOutputParameters());

        if (result.isEmpty()) {
            return BigInteger.ZERO;
        }

        return (BigInteger) result.get(0).getValue();
    }

}