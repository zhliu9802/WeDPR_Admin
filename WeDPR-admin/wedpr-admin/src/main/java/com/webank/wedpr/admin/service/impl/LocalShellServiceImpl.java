package com.webank.wedpr.admin.service.impl;

import com.webank.wedpr.admin.common.Utils;
import com.webank.wedpr.admin.config.WedprCertConfig;
import com.webank.wedpr.admin.service.LocalShellService;
import com.webank.wedpr.common.protocol.CertScriptCmdEnum;
import com.webank.wedpr.common.utils.Constant;
import com.webank.wedpr.common.utils.WeDPRException;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Created by caryliao on 2024/8/24 20:17 */
@Service
@Slf4j
public class LocalShellServiceImpl implements LocalShellService {
    @Autowired private WedprCertConfig wedprCertConfig;

    private void validateInputs(String certScript, String rootCertPath, String csrPath, long days)
            throws WeDPRException {
        if (!isValidFilePath(certScript)) {
            throw new WeDPRException("Invalid cert script path");
        }
        if (!isValidFilePath(rootCertPath)) {
            throw new WeDPRException("Invalid root cert path");
        }
        if (!isValidFilePath(csrPath)) {
            throw new WeDPRException("Invalid csr path");
        }
        if (days <= 0 || days > 3650) { // 限制最大10年
            throw new WeDPRException("Invalid number of days");
        }
    }

    private boolean isValidFilePath(String path) {
        try {
            if (path == null || path.trim().isEmpty()) {
                return false;
            }

            // 检查危险字符
            if (path.contains(";")
                    || path.contains("|")
                    || path.contains("&")
                    || path.contains(">")
                    || path.contains("<")) {
                return false;
            }

            Path normalizedPath = Paths.get(path).normalize();
            File file = normalizedPath.toFile();
            return file.exists() && file.canRead();
        } catch (Exception e) {
            log.error("Path validation failed", e);
            return false;
        }
    }

    @Override
    public boolean buildAuthorityCsrToCrt(String agencyName, String csrPath, long days) {
        try {
            log.info("buildAuthorityCsrToCrt start");
            String certScriptDir = wedprCertConfig.getCertScriptDir();
            String certScript = wedprCertConfig.getCertScript();
            String rootCertPath = wedprCertConfig.getRootCertPath();

            // 记录参数
            log.info(
                    "buildAuthorityCsrToCrt params - certScriptDir:{}, certScript:{}, rootCertPath:{}, csrPath:{}, days:{}",
                    certScriptDir,
                    certScript,
                    rootCertPath,
                    csrPath,
                    days);

            // 验证输入参数
            validateInputs(certScript, rootCertPath, csrPath, days);

            // 使用List构建命令
            List<String> command =
                    Arrays.asList(
                            certScript,
                            CertScriptCmdEnum.CSR_TO_CRT.getName(),
                            rootCertPath,
                            csrPath,
                            String.valueOf(days));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(certScriptDir));

            // 使用改进的executeScript方法
            String result = executeScript(processBuilder);

            log.info("buildAuthorityCsrToCrt result: {}", result);
            return !Utils.isEmpty(result) && result.contains(Constant.CERT_SCRIPT_EXECUTE_OK);
        } catch (Exception e) {
            log.error("buildAuthorityCsrToCrt error", e);
            return false;
        }
    }

    public String executeScript(ProcessBuilder processBuilder) throws WeDPRException {
        Process process = null;
        try {
            process = processBuilder.start();
            log.info("executeScript start with command: {}", processBuilder.command());

            // 添加超时机制
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new WeDPRException("Command execution timed out");
            }

            // 检查执行结果
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                BufferedReader errorReader =
                        new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorOutput = errorReader.lines().reduce("", (a, b) -> a + "\n" + b);
                log.error(
                        "Command execution failed with exit code: {} and error: {}",
                        exitCode,
                        errorOutput);
                throw new WeDPRException("Command execution failed");
            }

            // 读取正常输出
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            return output.toString();
        } catch (Exception e) {
            log.error("executeScript error", e);
            throw new WeDPRException("executeScript error: " + e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
