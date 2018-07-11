package examples;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerIbmX3550XeonX5670;

/**
 * In this class the specifications of the Cloudlets, Containers, VMs and Hosts are coded.
 * Regarding to the hosts, the powermodel of each type of the hosts are all included in this class.
 */
public class ConstantsExamples {

    /**
     * Simulation  parameters including the interval and limit
     */
    public static final boolean ENABLE_OUTPUT = true;
    public static final boolean OUTPUT_CSV = false;
    public static final double SCHEDULING_INTERVAL = 300.0D;
    public static final double SIMULATION_LIMIT = 87400.0D;
    /**
     * Cloudlet specs
     */
    public static final int CLOUDLET_LENGTH = 30;
    public static final int CLOUDLET_PES = 1;

    /**
     * Startup delay for VMs and the containers are mentioned here.
     */
    public static final double CONTAINER_STARTTUP_DELAY = 0.4;//the amount is in seconds
    public static final double VM_STARTTUP_DELAY = 100;//the amount is in seconds

    /**
     * The available virtual machine types along with the specs.
     */

//    public static final int VM_TYPES = 4;
//    public static final double VM_MIPS = 37274/ 2;
//    public static final int VM_PES = 2;
//    public static final float VM_RAM = (float)1024;//**MB*
//    public static final int VM_BW = 100000;
//    public static final int VM_SIZE = 2500;
    public static final double VM_MIPS = 2000;
    public static final int VM_PES = 1;
    public static final float VM_RAM = (float)8192;//**MB*
    public static final int VM_BW = 1000000;
    public static final int VM_SIZE = 2500;

    /**
     * The available types of container along with the specs.
     */

//    public static final int CONTAINER_TYPES = 3;
//    public static final int CONTAINER_MIPS = 4658;
//    public static final int CONTAINER_PES = 1;
//    public static final int CONTAINER_RAM = 128;
//    public static final int CONTAINER_BW = 2500;
    public static final int CONTAINER_MIPS = 500;
    public static final int CONTAINER_PES = 1;
    public static final int CONTAINER_RAM = 1364;
    public static final int CONTAINER_BW = 2500;

    /**
     * The available types of hosts along with the specs.
     */

//    public static final int HOST_TYPES = 3;
//    public static final int HOST_MIPS = 37274;
//    public static final int HOST_PES = 4;
//    public static final int HOST_RAM = 65536;
//    public static final int HOST_BW = 1000000;
//    public static final int HOST_STORAGE = 1000000;
//    public static final PowerModel[] HOST_POWER = new PowerModel[]{new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
//            new PowerModelSpecPowerHpProLiantMl110G5Xeon3075(), new PowerModelSpecPowerIbmX3550XeonX5670()};
    public static final int HOST_MIPS = 2000;
    public static final int HOST_PES = 2;
    public static final int HOST_RAM = 8192;
    public static final int HOST_BW = 1000000;
    public static final int HOST_STORAGE = 250000000;
    public static final PowerModel[] HOST_POWER = new PowerModel[]{new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
            new PowerModelSpecPowerHpProLiantMl110G5Xeon3075(), new PowerModelSpecPowerIbmX3550XeonX5670()};

    /**
     * The population of hosts, containers, and VMs are specified.
     * The containers population is equal to the cloudlets population as each cloudlet is mapped to each container.
     * However, depending on the simulation scenario the container's population can also be different from cloudlet's
     * population.
     */


    public static final int NUMBER_HOSTS = 2; //2 //O NUMERO DE HOSTS TEM QUE SER NO MINIMO 4 VEZES MENOR QUE O NUMERO DE VMS
    public static final int NUMBER_VMS = 2; // 7 //O NUMERO DE HOSTS TEM QUE SER NO MINIMO 8 VEZES MENOR QUE O NUMERO DE CONTAINERS
    public static final int NUMBER_CLOUDLETS = 6; // 50

    public ConstantsExamples() {
    }
}