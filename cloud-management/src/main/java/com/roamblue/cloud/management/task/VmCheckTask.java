package com.roamblue.cloud.management.task;

import cn.hutool.cache.impl.LRUCache;
import com.roamblue.cloud.common.agent.VmInfoModel;
import com.roamblue.cloud.management.data.entity.HostEntity;
import com.roamblue.cloud.management.data.entity.VmEntity;
import com.roamblue.cloud.management.data.entity.VmStaticsEntity;
import com.roamblue.cloud.management.data.mapper.HostMapper;
import com.roamblue.cloud.management.data.mapper.VmMapper;
import com.roamblue.cloud.management.data.mapper.VmStatsMapper;
import com.roamblue.cloud.management.service.AgentService;
import com.roamblue.cloud.management.service.HostService;
import com.roamblue.cloud.management.service.LockService;
import com.roamblue.cloud.management.service.VncService;
import com.roamblue.cloud.management.util.VmStatus;
import com.roamblue.cloud.management.util.LockKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class VmCheckTask extends AbstractTask {
    @Autowired
    private HostService hostService;
    @Autowired
    private LockService lockService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private VmMapper vmMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private VmStatsMapper vmStatsMapper;

    private LRUCache<Integer, VmStaticsEntity> cache = new LRUCache<>(100000, 60 * 1000);
    @Autowired
    private VncService vncService;

    @Override
    protected int getInterval() {
        return 10000;
    }

    @Override
    protected String getName() {
        return "VmCheckTask";
    }

    @Override
    protected void call() {
        List<HostEntity> list = hostMapper.selectAll();
        for (HostEntity hostInfo : list) {
            List<VmInfoModel> vmInfoList = agentService.getInstance(hostInfo.getHostUri()).getData();
            if (vmInfoList == null || vmInfoList.isEmpty()) {
                continue;
            }
            for (VmInfoModel vmInfo : vmInfoList) {
                VmEntity vm = vmMapper.findByName(vmInfo.getName());
                if (vm == null) {
                    agentService.destroyVm(hostInfo.getHostUri(), vmInfo.getName());
                    log.warn("未知的VM,自动关闭.name={}", vmInfo.getName());
                    continue;
                }
                lockService.tryRun(LockKeyUtil.getInstanceLockKey(vm.getId()), () -> {
                    if (!vm.getVmStatus().equalsIgnoreCase(VmStatus.STOPPED) && vm.getHostId().equals(hostInfo.getId())) {
                        return null;
                    }
//                    String pwd="password";


                    int id = vm.getId();
                    VmEntity find = vmMapper.selectById(id);
                    if (find == null) {
                        return null;
                    }
//                    pwd=JasyptPBEStringDecryptionCLI2.getVncPaassword(new String[]{"input="+find.getVncPassword(),"password="+pwd});
//                    if(!StringUtils.isEmpty(pwd)){
//                        find.setVncPort(vmInfo.getVnc());
//                        find.setVncPassword(pwd);
//                        vmMapper.updateById(find);
//                        vncService.addInstance(find.getClusterId(),find.getId(),hostInfo.getHostIp(),find.getVncPort(),pwd);
//                    }
                    if (find.getVmStatus().equalsIgnoreCase(VmStatus.STOPPED) || !find.getHostId().equals(hostInfo.getId())) {
                        log.warn("VM状态不一致，自动销毁", vmInfo.getName());
                        //如果运行机器和当前机器不一致，则直接销毁
                        agentService.destroyVm(hostInfo.getHostUri(), find.getVmName());
                    }
                    return null;
                }, 10, TimeUnit.SECONDS);

            }
        }

    }


//
//     public static final class JasyptPBEStringDecryptionCLI2 {
//
//        /*
//         * The required arguments for this CLI operation.
//         */
//        private static final String[][] VALID_REQUIRED_ARGUMENTS =
//                new String[][] {
//                        new String [] {
//                                ArgumentNaming.ARG_INPUT
//                        },
//                        new String [] {
//                                ArgumentNaming.ARG_PASSWORD
//                        }
//                };
//
//        /*
//         * The optional arguments for this CLI operation.
//         */
//        private static final String[][] VALID_OPTIONAL_ARGUMENTS =
//                new String[][] {
//                        new String [] {
//                                ArgumentNaming.ARG_VERBOSE
//                        },
//                        new String [] {
//                                ArgumentNaming.ARG_ALGORITHM
//                        },
//                        new String [] {
//                                ArgumentNaming.ARG_KEY_OBTENTION_ITERATIONS
//                        },
//                        new String [] {
//                                ArgumentNaming.ARG_SALT_GENERATOR_CLASS_NAME
//                        },
//                        new String [] {
//                                ArgumentNaming.ARG_PROVIDER_NAME
//                        },
//                        new String [] {
//                                ArgumentNaming.ARG_PROVIDER_CLASS_NAME
//                        },
//                        new String [] {
//                                ArgumentNaming.ARG_STRING_OUTPUT_TYPE
//                        },
//                        new String[] {
//                                ArgumentNaming.ARG_IV_GENERATOR_CLASS_NAME
//                        }
//                };
//
//
//        /**
//         * <p>
//         * CLI execution method.
//         * </p>
//         *
//         * @param args the command execution arguments
//         */
//        public static String getVncPaassword(final String[] args) {
//
//            boolean verbose = CLIUtils.getVerbosity(args);
//
//            try {
//
//                String applicationName = null;
//                String[] arguments = null;
//                if (args[0] == null || args[0].indexOf("=") != -1) {
//                    applicationName = org.jasypt.intf.cli.JasyptPBEStringDecryptionCLI.class.getName();
//                    arguments = args;
//                } else {
//                    applicationName = args[0];
//                    arguments = new String[args.length - 1];
//                    System.arraycopy(args, 1, arguments, 0, args.length - 1);
//                }
//
//                final Properties argumentValues =
//                        CLIUtils.getArgumentValues(
//                                applicationName, arguments,
//                                VALID_REQUIRED_ARGUMENTS, VALID_OPTIONAL_ARGUMENTS);
//
//                CLIUtils.showEnvironment(verbose);
//
//                final JasyptStatelessService service = new JasyptStatelessService();
//
//                final String input = argumentValues.getProperty(ArgumentNaming.ARG_INPUT);
//
//                CLIUtils.showArgumentDescription(argumentValues, verbose);
//
//                final String result =
//                        service.decrypt(
//                                input,
//                                argumentValues.getProperty(ArgumentNaming.ARG_PASSWORD),
//                                null,
//                                null,
//                                argumentValues.getProperty(ArgumentNaming.ARG_ALGORITHM),
//                                null,
//                                null,
//                                argumentValues.getProperty(ArgumentNaming.ARG_KEY_OBTENTION_ITERATIONS),
//                                null,
//                                null,
//                                argumentValues.getProperty(ArgumentNaming.ARG_SALT_GENERATOR_CLASS_NAME),
//                                null,
//                                null,
//                                argumentValues.getProperty(ArgumentNaming.ARG_PROVIDER_NAME),
//                                null,
//                                null,
//                                argumentValues.getProperty(ArgumentNaming.ARG_PROVIDER_CLASS_NAME),
//                                null,
//                                null,
//                                argumentValues.getProperty(ArgumentNaming.ARG_STRING_OUTPUT_TYPE),
//                                null,
//                                null,
//                                argumentValues.getProperty(ArgumentNaming.ARG_IV_GENERATOR_CLASS_NAME),
//                                null,
//                                null);
//
//                CLIUtils.showOutput(result, verbose);
//                return result;
//
//            } catch (Throwable t) {
//                CLIUtils.showError(t, verbose);
//                return "";
//            }
//
//        }
//
//
//        /*
//         * Instantiation is forbidden.
//         */
//        private JasyptPBEStringDecryptionCLI2() {
//            super();
//        }
//
//    }
//    public static final class CLIUtils {
//
//
//        /*
//         * Renders the execution environment.
//         */
//        static void showEnvironment(final boolean verbose) {
//
//            if (verbose) {
//                System.out.println("\n----ENVIRONMENT-----------------\n");
//                System.out.println("Runtime: " +
//                        System.getProperty("java.vm.vendor") + " " +
//                        System.getProperty("java.vm.name") + " " +
//                        System.getProperty("java.vm.version") + " ");
//                System.out.println("\n");
//            }
//
//        }
//
//
//        /*
//         * Renders the command arguments as accepted for execution.
//         */
//        static void showArgumentDescription(final Properties argumentValues, final boolean verbose) {
//
//            if (verbose) {
//                System.out.println("\n----ARGUMENTS-------------------\n");
//                final Iterator entriesIter = argumentValues.entrySet().iterator();
//                while (entriesIter.hasNext()) {
//                    final Map.Entry entry = (Map.Entry) entriesIter.next();
//                    System.out.println(
//                            entry.getKey() + ": " + entry.getValue());
//                }
//                System.out.println("\n");
//            }
//
//        }
//
//
//        /*
//         * Renders the command output.
//         */
//        static void showOutput(final String output, final boolean verbose) {
//
//            if (verbose) {
//                System.out.println("\n----OUTPUT----------------------\n");
//                System.out.println(output);
//                System.out.println("\n");
//            } else {
//                System.out.println(output);
//            }
//
//        }
//
//
//        /*
//         * Renders an error occurred during execution.
//         */
//        static void showError(final Throwable t, final boolean verbose) {
//
//            if (verbose) {
//
//                System.err.println("\n----ERROR-----------------------\n");
//                if (t instanceof EncryptionOperationNotPossibleException) {
//                    System.err.println(
//                            "Operation not possible (Bad input or parameters)");
//                } else {
//                    if (t.getMessage() != null) {
//                        System.err.println(t.getMessage());
//                    } else {
//                        System.err.println(t.getClass().getName());
//                    }
//                }
//                System.err.println("\n");
//
//            } else {
//
//                System.err.print("ERROR: ");
//                if (t instanceof EncryptionOperationNotPossibleException) {
//                    System.err.println(
//                            "Operation not possible (Bad input or parameters)");
//                } else {
//                    if (t.getMessage() != null) {
//                        System.err.println(t.getMessage());
//                    } else {
//                        System.err.println(t.getClass().getName());
//                    }
//                }
//
//            }
//
//        }
//
//
//        /*
//         * Defines whether the user has turned verbosity off or not.
//         */
//        static boolean getVerbosity(final String[] args) {
//            for (int i = 0; i < args.length; i++) {
//                final String key = CommonUtils.substringBefore(args[i], "=");
//                final String value = CommonUtils.substringAfter(args[i], "=");
//                if (CommonUtils.isEmpty(key) || CommonUtils.isEmpty(value)) {
//                    continue;
//                }
//                if (ArgumentNaming.ARG_VERBOSE.equals(key)) {
//                    final Boolean verbosity =
//                            CommonUtils.getStandardBooleanValue(value);
//                    return (verbosity != null? verbosity.booleanValue() : false);
//                }
//            }
//            return true;
//        }
//
//
//        /*
//         * Extracts the argument values and checks its wellformedness.
//         */
//        static Properties getArgumentValues(final String appName, final String[] args,
//                                            final String[][] requiredArgNames, final String[][] optionalArgNames) {
//
//            final Set argNames = new HashSet();
//            for (int i = 0; i < requiredArgNames.length; i++) {
//                argNames.addAll(Arrays.asList(requiredArgNames[i]));
//            }
//            for (int i = 0; i < optionalArgNames.length; i++) {
//                argNames.addAll(Arrays.asList(optionalArgNames[i]));
//            }
//
//            final Properties argumentValues = new Properties();
//            for (int i = 0; i < args.length; i++) {
//                final String key = CommonUtils.substringBefore(args[i], "=");
//                final String value = CommonUtils.substringAfter(args[i], "=");
//                if (CommonUtils.isEmpty(key) || CommonUtils.isEmpty(value)) {
//                    throw new IllegalArgumentException("Bad argument: " + args[i]);
//                }
//                if (argNames.contains(key)) {
//                    if (value.startsWith("\"") && value.endsWith("\"")) {
//                        argumentValues.setProperty(
//                                key,
//                                value.substring(1, value.length() - 1));
//                    } else {
//                        argumentValues.setProperty(key, value);
//                    }
//                } else {
//                    throw new IllegalArgumentException("Bad argument: " + args[i]);
//                }
//            }
//
//            //Check for all required arguments
//            for (int i = 0; i < requiredArgNames.length; i++) {
//                boolean found = false;
//                for (int j = 0; j < requiredArgNames[i].length; j++) {
//                    if (argumentValues.containsKey(requiredArgNames[i][j])) {
//                        found = true;
//                    }
//                }
//                if (!found) {
//                    showUsageAndExit(
//                            appName, requiredArgNames, optionalArgNames);
//                }
//            }
//            return argumentValues;
//
//        }
//
//
//        /*
//         * Renders the usage instructions and exits with error.
//         */
//       public static void showUsageAndExit(final String appName,
//                                     final String[][] requiredArgNames, final String[][] optionalArgNames) {
//
//            System.err.println("\nUSAGE: " + appName + " [ARGUMENTS]\n");
//            System.err.println("  * Arguments must apply to format:\n");
//            System.err.println(
//                    "      \"arg1=value1 arg2=value2 arg3=value3 ...\"");
//            System.err.println();
//            System.err.println("  * Required arguments:\n");
//            for (int i = 0; i < requiredArgNames.length; i++) {
//                System.err.print("      ");
//                if (requiredArgNames[i].length == 1) {
//                    System.err.print(requiredArgNames[i][0]);
//                } else {
//                    System.err.print("(");
//                    for (int j = 0; j < requiredArgNames[i].length; j++) {
//                        if (j > 0) {
//                            System.err.print(" | ");
//                        }
//                        System.err.print(requiredArgNames[i][j]);
//                    }
//                    System.err.print(")");
//                }
//                System.err.println();
//            }
//            System.err.println();
//            System.err.println("  * Optional arguments:\n");
//            for (int i = 0; i < optionalArgNames.length; i++) {
//                System.err.print("      ");
//                if (optionalArgNames[i].length == 1) {
//                    System.err.print(optionalArgNames[i][0]);
//                } else {
//                    System.err.print("(");
//                    for (int j = 0; j < optionalArgNames[i].length; j++) {
//                        if (j > 0) {
//                            System.err.print(" | ");
//                        }
//                        System.err.print(optionalArgNames[i][j]);
//                    }
//                    System.err.print(")");
//                }
//                System.err.println();
//            }
//            System.exit(1);
//
//        }
//
//
//        /*
//         * Instantiation is forbidden.
//         */
//        private CLIUtils() {
//            super();
//        }
//
//    }
//    public static final class ArgumentNaming {
//
//
//        static final String ARG_VERBOSE = "verbose";
//
//        static final String ARG_INPUT = "input";
//
//        static final String ARG_PASSWORD = "password";
//
//        static final String ARG_ALGORITHM = "algorithm";
//
//        static final String ARG_ITERATIONS = "iterations";
//
//        static final String ARG_KEY_OBTENTION_ITERATIONS =
//                "keyObtentionIterations";
//
//        static final String ARG_SALT_SIZE_BYTES = "saltSizeBytes";
//
//        static final String ARG_SALT_GENERATOR_CLASS_NAME =
//                "saltGeneratorClassName";
//
//        static final String ARG_IV_GENERATOR_CLASS_NAME =
//                "ivGeneratorClassName";
//
//        static final String ARG_PROVIDER_CLASS_NAME = "providerClassName";
//
//        static final String ARG_PROVIDER_NAME = "providerName";
//
//        static final String ARG_INVERT_POSITION_OF_SALT_IN_MESSAGE_BEFORE_DIGESTING =
//                "invertPositionOfSaltInMessageBeforeDigesting";
//
//        static final String ARG_INVERT_POSITION_OF_PLAIN_SALT_IN_ENCRYPTION_RESULTS =
//                "invertPositionOfPlainSaltInEncryptionResults";
//
//        static final String ARG_USE_LENIENT_SALT_SIZE_CHECK =
//                "useLenientSaltSizeCheck";
//
//        static final String ARG_UNICODE_NORMALIZATION_IGNORED =
//                "unicodeNormalizationIgnored";
//
//        static final String ARG_STRING_OUTPUT_TYPE =
//                "stringOutputType";
//
//        static final String ARG_PREFIX = "prefix";
//
//        static final String ARG_SUFFIX = "suffix";
//
//
//
//        // Instantiation is not allowed
//        private ArgumentNaming() {
//            super();
//        }
//
//    }
}
