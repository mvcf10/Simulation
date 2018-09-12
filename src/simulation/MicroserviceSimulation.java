package simulation;

/*
 Container simulation example
 */

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelNull;
import org.cloudbus.cloudsim.UtilizationModelPlanetLabInMemory;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerBwProvisionerSimple;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerPe;
import org.cloudbus.cloudsim.container.containerProvisioners.ContainerRamProvisionerSimple;
import org.cloudbus.cloudsim.container.containerProvisioners.CotainerPeProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmBwProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPe;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmPeProvisionerSimple;
import org.cloudbus.cloudsim.container.containerVmProvisioners.ContainerVmRamProvisionerSimple;
import org.cloudbus.cloudsim.container.core.*;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.HostSelectionPolicy;
import org.cloudbus.cloudsim.container.hostSelectionPolicies.HostSelectionPolicyFirstFit;
import org.cloudbus.cloudsim.container.resourceAllocatorMigrationEnabled.PowerContainerVmAllocationPolicyMigrationAbstractHostSelection;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.ContainerVmAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.PowerContainerAllocationPolicy;
import org.cloudbus.cloudsim.container.resourceAllocators.PowerContainerAllocationPolicySimple;
import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.container.schedulers.ContainerSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.schedulers.ContainerVmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.container.utils.IDs;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicy;
import org.cloudbus.cloudsim.container.vmSelectionPolicies.PowerContainerVmSelectionPolicyMaximumUsage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.util.WorkloadFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MicroserviceSimulation {

    private static List<ContainerCloudlet> cloudletList;
    private static List<ContainerVm> vmList;
    private static List<Container> containerList;
    private static List<ContainerHost> hostList;

    public static void main(String[] args) {
        System.out.println("Starting Simulation...");
        try {
        	try {
				OutputStream os = new FileOutputStream(".//result//ResultContainerSimulationDellWorkload.txt");
				//OutputStream os = new FileOutputStream(".//result//ResultContainerSimulationNasaWorkload.txt");
				Log.setOutput(os);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
        	
            int num_user = 1; //Numero de usuarios
            Calendar calendar = Calendar.getInstance(); //Inicializa o calendario
            boolean trace_flag = false; //Desativa o event tracing

            /*Inicializa o cloudsim*/
            CloudSim.init(num_user, calendar, trace_flag); 
            
            /*Define politica de alocação de containers*/
            ContainerAllocationPolicy containerAllocationPolicy = new PowerContainerAllocationPolicySimple() {	
            	@Override
            	public List<Map<String, Object>> optimizeAllocation(List<? extends Container> arg0) {
            		return null;
            	}
			}; 

            /*Define politica de seleção de VM*/
            PowerContainerVmSelectionPolicy vmSelectionPolicy = new PowerContainerVmSelectionPolicyMaximumUsage();

            /*Define politica de selecao de host*/
            HostSelectionPolicy hostSelectionPolicy = new HostSelectionPolicyFirstFit();

            /*Define os limites para selecao de sub-utilizado e sobre-utilizado*/
            double overUtilizationThreshold = 0.80;
            double underUtilizationThreshold = 0.70;

            /*Criacao da lista de hosts, considerando seu numero e os tipos*/
            hostList = new ArrayList<ContainerHost>();
            hostList = createHostList(ConstantsExamples.NUMBER_HOSTS);

            /*Define politica de alocacao de containers*/
            ContainerVmAllocationPolicy vmAllocationPolicy = new
                    PowerContainerVmAllocationPolicyMigrationAbstractHostSelection(hostList, vmSelectionPolicy,
                    hostSelectionPolicy, overUtilizationThreshold, underUtilizationThreshold);

            /*Fator de sobre-reserva para alocacao de containers a VM's*/
            int overBookingFactor = 80;
            ContainerDatacenterBroker broker = createBroker(overBookingFactor);
            int brokerId = broker.getId();
            
            /*Endereco para logging das estatisticas das VM's, containers e datacenter*/
            String logAddress = ".//result//LogContainerSimulation.txt";
            
            //Cria datacenter
            @SuppressWarnings("unused")
			PowerContainerDatacenter datacenter0 = (PowerContainerDatacenter) createDatacenter("Datacenter",
                    PowerContainerDatacenterCM.class, hostList, vmAllocationPolicy, containerAllocationPolicy,
                    getExperimentName("ContainerCloudSimExample-1", String.valueOf(overBookingFactor)),
                    ConstantsExamples.SCHEDULING_INTERVAL, logAddress,
                    ConstantsExamples.VM_STARTTUP_DELAY, ConstantsExamples.CONTAINER_STARTTUP_DELAY);

            /*Criacao da lista de cloudlet, container e VM's*/
            vmList = new ArrayList<ContainerVm>();
            cloudletList = new ArrayList<ContainerCloudlet>();
            vmList = createVmList(brokerId, ConstantsExamples.NUMBER_VMS);
            containerList = createContainerList(brokerId, ConstantsExamples.NUMBER_CONTAINERS);
            cloudletList = createContainerCloudletList2(brokerId, ConstantsExamples.NUMBER_CLOUDLETS);

		
            /*Submeter listas ao broker*/
            broker.submitVmList(vmList);
            broker.submitContainerList(containerList);
//            broker.submitCloudletList(cloudletList.subList(0, containerList.size()));
            broker.submitCloudletList(cloudletList);
            
            /*Determinar o tempo do fim da simulacao de acordo com o workload*/
            CloudSim.terminateSimulation(86400);

            /*Iniciando a simulacao */
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date dateStarted = new Date();
			Log.printLine("========== OUTPUT ==========");
			Log.printLine("Started at:" + (dateFormat.format(dateStarted)));
            
			CloudSim.startSimulation();

            CloudSim.stopSimulation();
            
            /*Printando os resultados quando a simulacao e terminada*/
//            List<ContainerCloudlet> subList = broker.getCloudletSubmittedList();
//            List<ContainerCloudlet> list = broker.getCloudletList();
//            List<ContainerCloudlet> newList = broker.getCloudletList(); //Cloudlets Falhos
            System.out.println();
            System.out.println(broker.getCloudletList().size());
            System.out.println(broker.getCloudletSubmittedList().size());
            System.out.println(broker.getCloudletReceivedList().size());
            List<ContainerCloudlet> newList2 = broker.getCloudletReceivedList();
//            List<ContainerCloudlet> newList3 = broker.getCloudletSubmittedList();
//            for(ContainerCloudlet c : newList) {
//            	System.out.println(c);
//            }
//            System.out.println(newList.size());
//            System.out.println(newList2.size());
//            System.out.println(newList3.size());
            printCloudletList(newList2);
            
            Date dateFinished = new Date();
			Log.printLine("Finished at:" + (dateFormat.format(dateFinished)));
			Log.printLine("============================");
			
			System.out.println("Simulation finished!");

            //Log.printLine("ContainerCloudSimExample1 finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    /*Cria nomes especificos para o experimento, usado para nomear os enderecos das pastas de Log*/
    private static String getExperimentName(String... args) {
        StringBuilder experimentName = new StringBuilder();

        for (int i = 0; i < args.length; ++i) {
            if (!args[i].isEmpty()) {
                if (i != 0) {
                    experimentName.append("_");
                }

                experimentName.append(args[i]);
            }
        }

        return experimentName.toString();
    }

    /*Cria o broker*/
    private static ContainerDatacenterBroker createBroker(int overBookingFactor) {

        ContainerDatacenterBroker broker = null;

        try {
            broker = new ContainerDatacenterBroker("Broker", overBookingFactor);
        } catch (Exception var2) {
            var2.printStackTrace();
            System.exit(0);
        }

        return broker;
    }

    /*Printa os cloudlets*/
    private static void printCloudletList(List<ContainerCloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + "Time" + indent
                + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatusString() == "Success") {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent
                        + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
    
    /*Cria VM e adiciona a lista*/
    private static ArrayList<ContainerVm> createVmList(int brokerId, int containerVmsNumber) {
        ArrayList<ContainerVm> containerVms = new ArrayList<ContainerVm>();

        for (int i = 0; i < containerVmsNumber; ++i) {
            ArrayList<ContainerPe> peList = new ArrayList<ContainerPe>();
            for (int j = 0; j < ConstantsExamples.VM_PES[0]; ++j) {
                peList.add(new ContainerPe(j,
                        new CotainerPeProvisionerSimple((double) ConstantsExamples.VM_MIPS[0])));
            }
            containerVms.add(new PowerContainerVm(IDs.pollId(ContainerVm.class), brokerId,
                    (double) ConstantsExamples.VM_MIPS[0], (float) ConstantsExamples.VM_RAM[0],
                    ConstantsExamples.VM_BW, ConstantsExamples.VM_SIZE, "Xen",
                    new ContainerSchedulerTimeSharedOverSubscription(peList),
                    new ContainerRamProvisionerSimple(ConstantsExamples.VM_RAM[0]),
                    new ContainerBwProvisionerSimple(ConstantsExamples.VM_BW),
                    peList, ConstantsExamples.SCHEDULING_INTERVAL));
        }
        System.out.println("VM's: "+containerVms.size());
        return containerVms;
    }

    /*Cria a lista de host considerando as aspectos listado em {@link ConstantsExamples}*/
    public static List<ContainerHost> createHostList(int hostsNumber) {
        ArrayList<ContainerHost> hostList = new ArrayList<ContainerHost>();
        for (int i = 0; i < hostsNumber; ++i) {
            int hostType = i / (int) Math.ceil((double) hostsNumber / 3.0D);
            ArrayList<ContainerVmPe> peList = new ArrayList<ContainerVmPe>();
            for (int j = 0; j < ConstantsExamples.HOST_PES[0]; ++j) {
                peList.add(new ContainerVmPe(j,
                        new ContainerVmPeProvisionerSimple((double) ConstantsExamples.HOST_MIPS[0])));
            }

            hostList.add(new PowerContainerHostUtilizationHistory(IDs.pollId(ContainerHost.class),
                    new ContainerVmRamProvisionerSimple(ConstantsExamples.HOST_RAM[0]),
                    new ContainerVmBwProvisionerSimple(ConstantsExamples.HOST_BW), ConstantsExamples.HOST_STORAGE, peList,
                    new ContainerVmSchedulerTimeSharedOverSubscription(peList),
                    ConstantsExamples.HOST_POWER[hostType]));
        }
        System.out.println("Hosts: "+hostList.size());
        return hostList;
    }

    /*Cria datacenter*/
    public static ContainerDatacenter createDatacenter(String name, Class<? extends ContainerDatacenter> datacenterClass,
                                                       List<ContainerHost> hostList,
                                                       ContainerVmAllocationPolicy vmAllocationPolicy,
                                                       ContainerAllocationPolicy containerAllocationPolicy,
                                                       String experimentName, double schedulingInterval, String logAddress, double VMStartupDelay,
                                                       double ContainerStartupDelay) throws Exception {
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.1;
        double costPerBw = 0.1;
        ContainerDatacenterCharacteristics characteristics = new
                ContainerDatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage,
                costPerBw);
        ContainerDatacenter datacenter = new PowerContainerDatacenterCM(name, characteristics, vmAllocationPolicy,
                containerAllocationPolicy, new LinkedList<Storage>(), schedulingInterval, experimentName, logAddress,
                VMStartupDelay, ContainerStartupDelay);

        return datacenter;
    }

    /*Cria o container para hostear os cloudlets e linka-los*/
    public static List<Container> createContainerList(int brokerId, int containersNumber) {
        ArrayList<Container> containers = new ArrayList<Container>();
        for (int i = 0; i < containersNumber; ++i) {
            containers.add(new PowerContainer(IDs.pollId(Container.class), brokerId, (double) ConstantsExamples.CONTAINER_MIPS[0], ConstantsExamples.
                    CONTAINER_PES[0], ConstantsExamples.CONTAINER_RAM[0], ConstantsExamples.CONTAINER_BW, 0L, "Xen",
                    new ContainerCloudletSchedulerDynamicWorkload(ConstantsExamples.CONTAINER_MIPS[0], ConstantsExamples.CONTAINER_PES[0]), ConstantsExamples.SCHEDULING_INTERVAL));
        }
        System.out.println("Containers: "+containers.size());
        return containers;
    }

    /*Cria a lista de cloudlets que rodara nos containers*/
    public static List<ContainerCloudlet> createContainerCloudletList(int brokerId)
            throws FileNotFoundException {
    	ArrayList<ContainerCloudlet> containerCloudletList = new ArrayList<ContainerCloudlet>();
    	List<Cloudlet> cloudletList;
		WorkloadFileReader workloadFileReader = new WorkloadFileReader(".//workload//DellWorkload.swf", 1);
		cloudletList = workloadFileReader.generateWorkload();
		for (Cloudlet cldt : cloudletList) {
			cldt.setUserId(0);
		}
		for (Cloudlet cldt : cloudletList) {
			ContainerCloudlet cloudlet = null;
			try {
				cloudlet = new ContainerCloudlet(IDs.pollId(ContainerCloudlet.class), 300, cldt.getNumberOfPes(),
						300, 300, cldt.getUtilizationModelCpu(), 
						cldt.getUtilizationModelRam(), cldt.getUtilizationModelBw());
			} catch (Exception var13) {
				var13.printStackTrace();
				System.exit(0);
			}
			cloudlet.setUserId(brokerId);
			containerCloudletList.add(cloudlet);
		}
		System.out.println("Dell Cloudlets: "+containerCloudletList.size());
        return containerCloudletList;
    }
    
    public static List<ContainerCloudlet> createContainerCloudletList2(int brokerId, int numberOfCloudlets)
            throws FileNotFoundException {
    	String inputFolderName = ".//workload/planetlab";
        ArrayList<ContainerCloudlet> containerCloudletList = new ArrayList<ContainerCloudlet>();
        
        List<Cloudlet> cloudletList;
        WorkloadFileReader workloadFileReader = new WorkloadFileReader(".//workload//DellWorkload.swf", 1);
        //WorkloadFileReader workloadFileReader = new WorkloadFileReader(".//workload//NASA-iPSC-1993-3.swf", 1);
        cloudletList = workloadFileReader.generateWorkload();
		for (Cloudlet cldt : cloudletList) {
			cldt.setUserId(0);
		}
		
        UtilizationModelFull utilizationModelFull = new UtilizationModelFull();
        java.io.File inputFolder1 = new java.io.File(inputFolderName);
        java.io.File[] files1 = inputFolder1.listFiles();
        int createdCloudlets = 0;
        
        for (java.io.File aFiles1 : files1) {
            java.io.File inputFolder = new java.io.File(aFiles1.toString());
            java.io.File[] files = inputFolder.listFiles();
            for (int i = 0; i < files.length; ++i) {
                if (createdCloudlets < numberOfCloudlets) {
                	Cloudlet cldt = cloudletList.get(i);
                    ContainerCloudlet cloudlet = null;
                    try {
                        cloudlet = new ContainerCloudlet(IDs.pollId(ContainerCloudlet.class), 300, cldt.getNumberOfPes(),
                                cldt.getCloudletFileSize(), cldt.getCloudletOutputSize(), 
                                new UtilizationModelPlanetLabInMemoryExtended(files[i].getAbsolutePath(), 300.0D),
                                utilizationModelFull, utilizationModelFull);
                    } catch (Exception var13) {
                        var13.printStackTrace();
                        System.exit(0);
                    }
                    cloudlet.setUserId(brokerId);
                    containerCloudletList.add(cloudlet);
                    createdCloudlets++;
                } else {
                	System.out.println("Cloudlets: "+containerCloudletList.size());
                    return containerCloudletList;
                }
            }
        }
        System.out.println("Cloudlets: "+containerCloudletList.size());
        return containerCloudletList;
    }
}