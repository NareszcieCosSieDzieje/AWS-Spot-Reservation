package client;

import client.security.SecurePassword;
import com.datastax.oss.driver.api.core.PagingIterable;
import models.daos.AWSSpotDao;
import models.daos.AZToEc2MappingDao;
import models.daos.Ec2InstanceDao;
import models.daos.SpotsReservedDao;
import models.entities.AWSSpot;
import models.entities.AZToEC2Mapping;
import models.entities.EC2Instance;
import models.entities.User;
import models.mappers.InventoryMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AwsConsoleInterface implements Runnable {

    private final InventoryMapper inventoryMapper;
    private int iterCount;
    private final int EXIT_CODE = 0;
    private User currUser = null;
    private File logsDir;

    public AwsConsoleInterface(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    // RUNNABLE VERSION
    public AwsConsoleInterface(InventoryMapper inventoryMapper, int iterCount) {
        this.inventoryMapper = inventoryMapper;
        this.iterCount = iterCount;
    }

    public void startLoop() {
        int response;
        printHeader();
        while (!handleUserLogin()) {
            Scanner reader = new Scanner(System.in);
            System.out.print("Do you want to exit the program?[Y/N]");
            String choice = reader.nextLine().strip().toLowerCase();
            if (choice.equals("y")) {
                return;
            }
        }
        do {
            printMenu();
            response = getResponse();
            if (response == -1) {
                continue;
            }
            handleResponse(response);
            System.out.println();
        } while (response != EXIT_CODE);
    }

    private void printHeader() {
        System.out.println("=========================================================");
        System.out.println("                 AWS Spot Reservator 1.0                 ");
        System.out.println(" Copyright © by Paweł Kryczka and Krzysztof Charlikowski ");
        System.out.println("=========================================================");
        System.out.println();
    }

    private void printMenu() {
        System.out.printf("Logged in as: %s, Credits: %.2f%n", currUser.getName(), currUser.getCredits());
        System.out.println("1. Reserve a spot");
        System.out.println("2. Show your reserved spots");
        System.out.println("3. Show instance types");
        System.out.println("4. Run big traffic simulation");
        System.out.println("5. Release reserved spot");
        System.out.println(EXIT_CODE + ". Exit");
    }

    private boolean handleUserLogin() {
        String username;
        Scanner reader = new Scanner(System.in);
        do {
            System.out.print("Enter username: ");
            username = reader.nextLine().strip();
            if (username.isBlank()) {
                System.out.println("Username cannot be empty ''");
                continue;
            }
            break;
        } while (true);

        User user = inventoryMapper.userDao().find(username);
        if (user == null) {
            System.out.print("Specified user doesn't exist. Do you want to create it now?[Y/N] ");
            String response = reader.nextLine().strip().toLowerCase();
            if (response.equals("y")) {
                System.out.print("Enter password: ");
                String pass = reader.nextLine();
                user = new User();
                user.setName(username);
                user.setCredits(new BigDecimal(1000));
                String[] saltAndHash = SecurePassword.createSaltedHash(pass);
                user.setSalt(saltAndHash[0]);
                user.setPassword(saltAndHash[1]);
                inventoryMapper.userDao().save(user);
                System.out.printf("User %s created. You get %.2f credits for registering!%n", user.getName(), user.getCredits());
                currUser = user;
                return true;
            } else {
                return false;
            }
        } else {
            System.out.print("Enter password: ");
            String pass = reader.nextLine();
            if (SecurePassword.compareHashWithPassword(pass, user.getSalt(), user.getPassword())) {
                System.out.println("Access Granted");
                currUser = user;
                return true;
            } else {
                System.out.println("Access Denied");
                return false;
            }
        }
    }

    private int getResponse() {
        System.out.print("Your choice: ");
        Scanner reader = new Scanner(System.in);
        int choice = -1;
        try {
            choice = Integer.parseInt(reader.nextLine().strip());
        } catch (NumberFormatException e) {
            System.err.println("Choice invalid. Choice range [0, 5]");
        }
        if (choice < 0 || choice > 5) {
            System.err.println("Choice invalid. Choice range [0, 5]");
        }
        return choice;
    }

    private <T> T getSelectedItem(ArrayList<T> selectionList, String loopText) {
        Scanner reader = new Scanner(System.in);
        int indexStart = 0;
        int indexEnd = selectionList.size() - 1;
        int chosenIndex;
        final Runnable errorMessage = () ->
                System.out.println("Index must be a valid integer! The accepted range is [" + indexStart + ", " + indexEnd + "]");
        System.out.println("Press 'X' to exit");
        do {
            System.out.println(loopText);
            try {
                String input = reader.nextLine().strip();
                if (input.equals("X")) {
                    return null;
                }
                chosenIndex = Integer.parseInt(input);
            } catch(NumberFormatException e) {
                errorMessage.run();
                continue;
            }
            if (chosenIndex < indexStart || chosenIndex > indexEnd) {
                errorMessage.run();
                continue;
            }
            return selectionList.get(chosenIndex);
        } while (true);
    }

    private void optionsMenu() {
        ArrayList<AWSSpot> availableAWSSpotsForThePrice = new ArrayList<>();
        BigDecimal chosenMaxPrice;
        // inicjalizacja moze byc na zewnatrz bo reset tylko jak lista pusta
        do {
            PagingIterable<AZToEC2Mapping> azToEC2Mappings = this.inventoryMapper.azToEc2MappingDao().findAll();
            HashSet<String> foundRegionsSet = new HashSet<>();
            HashSet<String> foundAzsSet = new HashSet<>();
            HashSet<String> foundInstanceTypesSet = new HashSet<>();
            Spliterator<AZToEC2Mapping> azToEC2MappingSpliterator = azToEC2Mappings.spliterator();
            azToEC2MappingSpliterator.forEachRemaining((item) -> {
                foundRegionsSet.add(item.getRegion());
                foundAzsSet.add(item.getAz_name());
                foundInstanceTypesSet.add(item.getInstance_type());
            });

            ArrayList<String> foundAzsList = new ArrayList<>(foundAzsSet);
            ArrayList<String> foundRegionsList = new ArrayList<>(foundRegionsSet);
            ArrayList<String> foundInstanceTypesList = new ArrayList<>(foundInstanceTypesSet);

            ArrayList<ArrayList<String>> listOfLists = new ArrayList<>(Arrays.asList(foundRegionsList, foundAzsList, foundInstanceTypesList));
            HashMap<Integer, String> listPromptMap = new HashMap<>(Map.of(
                    0, "Choose region",
                    1, "Choose availability zone",
                    2, "Choose instance type"));

            HashMap<Integer, String> listIndexToKeyMapping = new HashMap<>(Map.of(
                    0, "region",
                    1, "az_name",
                    2, "instance_type"));


            HashMap<String, String> chosenElements = new HashMap<>(Map.of(
                    "region", "",
                    "az_name", "",
                    "instance_type", ""));

            for (ArrayList<String> list : listOfLists) {
                int listIndex = listOfLists.indexOf(list);
                if (listIndex < 0) {
                    System.err.println("Error getting the list index!");
                }
                int idx = 0;
                if (list.size() == 0) {
                    System.out.println("List of: " + listIndexToKeyMapping.get(listIndex) + " is empty.");
                    continue;
                }
                for (String elem : list) {
                    System.out.println(idx + ". " + elem);
                    idx += 1;
                }
                String chosenItem = this.getSelectedItem(list, listPromptMap.get(listIndex));
                String chosenElemKey = listIndexToKeyMapping.get(listIndex);
                chosenElements.put(chosenElemKey, Objects.requireNonNullElse(chosenItem, ""));
            }

            System.out.println("Chose the maximum price you are willing to pay for the Spot");
            System.out.println("Your credits: " + this.currUser.getCredits());

            AZToEC2Mapping azToEC2Mapping = this.inventoryMapper.azToEc2MappingDao().findByRegionAndInstanceTypeAndAzName(
                    chosenElements.get("region"),
                    chosenElements.get("instance_type"),
                    chosenElements.get("az_name"));
            BigDecimal minPrice = new BigDecimal("0.2");
            if (azToEC2Mapping != null) {
                minPrice = azToEC2Mapping.getMin_price();
            }

            BigDecimal maxPrice = this.currUser.getCredits();
            Scanner scanner = new Scanner(System.in);
            do {
                System.out.println("Choose your max price for the spot.");
                System.out.println("Min price is " + minPrice);
                try {
                    chosenMaxPrice = new BigDecimal(scanner.nextLine().strip());
                } catch (NumberFormatException e) {
                    System.out.println("Provided number was not valid.");
                    System.out.println("Accepting values from range: [" + minPrice + ", " + maxPrice + "]");
                    continue;
                }
                if (chosenMaxPrice.compareTo(minPrice) < 0 || chosenMaxPrice.compareTo(maxPrice) > 0) {
                    System.out.println("Provided number was not valid.");
                    System.out.println("Accepting values from range: [" + minPrice + ", " + maxPrice + "]");
                    continue;
                }
                break;
            } while (true);

            PagingIterable<AWSSpot> foundAWSSpots = this.inventoryMapper.awsSpotDao().findAll();
            Spliterator<AWSSpot> awsSpotsSpliterator = foundAWSSpots.spliterator();
            BigDecimal finalChosenMaxPrice = chosenMaxPrice;
            awsSpotsSpliterator.forEachRemaining((item) -> {
                if (item.getRegion().equals(chosenElements.get("region")) &&
                        item.getAz_name().equals(chosenElements.get("az_name")) &&
                        item.getInstance_type().equals(chosenElements.get("instance_type")) &&
                        item.getMax_price().compareTo(finalChosenMaxPrice) < 0 &&
                        !item.getUser_name().equals("")) {
                    availableAWSSpotsForThePrice.add(item);
                }
                else if (item.getRegion().equals(chosenElements.get("region")) &&
                        item.getAz_name().equals(chosenElements.get("az_name")) &&
                        item.getInstance_type().equals(chosenElements.get("instance_type")) &&
                        item.getMax_price().compareTo(finalChosenMaxPrice) <= 0 &&
                        item.getUser_name().equals("")) {
                    availableAWSSpotsForThePrice.add(item);
                }
            });

            availableAWSSpotsForThePrice.sort(AWSSpot.sortByMaxPrice);

            if (availableAWSSpotsForThePrice.size() == 0) {
                System.out.println("No AWSSpots available for the price: " + chosenMaxPrice);
                Scanner reader = new Scanner(System.in);
                do {
                    System.out.println("Press enter to start over.");
                    System.out.println("Press 'X' to exit.");
                    String input = reader.nextLine().strip();
                    if (input.isBlank()) {
                        System.out.println("Starting over.\n");
                        break;
                    } else if (input.equals("X")) {
                        System.out.println("Exiting.");
                        return;
                    }
                } while (true);
                continue;
            }
            break;
        } while (true);

        AWSSpot chosenSpot = availableAWSSpotsForThePrice.get(0);
        if (chosenSpot.getUser_name().equals("")) {
            this.inventoryMapper.spotsReservedDao().increment(
                    chosenSpot.getRegion(),
                    chosenSpot.getInstance_type(),
                    chosenSpot.getAz_name(),
                    1
            );
        }

        chosenSpot.setMax_price(chosenMaxPrice);
        chosenSpot.setUser_name(this.currUser.getName());
        this.currUser.setCredits(this.currUser.getCredits().subtract(chosenMaxPrice));

        this.inventoryMapper.userDao().update(this.currUser);
        this.inventoryMapper.awsSpotDao().update(chosenSpot);
    }

    private void releaseSpot(ArrayList<AWSSpot> reservedSpots ) {
        printSpots(reservedSpots, true);
        Scanner scanner = new Scanner(System.in);
        int maxIndex = reservedSpots.size() - 1;
        int chosenSpotID;
        do {
            System.out.println("Chose your number for the spot to be released");
            System.out.println("Press 'X' to exit");
            try {
                String input = scanner.nextLine().strip();
                if (input.equals("X")) {
                    System.out.println("Exiting.");
                    return;
                }
                chosenSpotID = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Provided number was not valid.");
                System.out.println("Accepting values from range: [" + 0 + ", " + maxIndex + "]");
                continue;
            }
            if (chosenSpotID < 0 || chosenSpotID > maxIndex) {
                System.out.println("Provided number was not valid.");
                System.out.println("Accepting values from range: [" + 0 + ", " + maxIndex + "]");
                continue;
            }
            break;
        } while (true);
        AWSSpot spotToBeReleased = reservedSpots.get(chosenSpotID);
        spotToBeReleased.setUser_name("");
        AZToEC2Mapping azToEC2Mapping = this.inventoryMapper.azToEc2MappingDao()
                 .findByRegionAndInstanceTypeAndAzName(
                         spotToBeReleased.getRegion(),
                         spotToBeReleased.getInstance_type(),
                         spotToBeReleased.getAz_name());
        spotToBeReleased.setMax_price(new BigDecimal("0.2"));
        if (azToEC2Mapping != null) {
            spotToBeReleased.setMax_price(azToEC2Mapping.getMin_price());
        }
        this.inventoryMapper.awsSpotDao().update(spotToBeReleased);
        if (!spotToBeReleased.getUser_name().equals("")) {
            this.inventoryMapper.spotsReservedDao().increment(
                    spotToBeReleased.getRegion(),
                    spotToBeReleased.getInstance_type(),
                    spotToBeReleased.getAz_name(),
                    -1
            );
        }
        System.out.println("Successfully released spot no.: " + chosenSpotID);
        printSpots(reservedSpots, true);
    }

    private void startTrafficSimulation(int iterCount) {
        AWSSpotDao awsSpotDao = inventoryMapper.awsSpotDao();
        SpotsReservedDao spotsReservedDao = inventoryMapper.spotsReservedDao();
        PagingIterable<AWSSpot> awsSpots = awsSpotDao.findAll();

        ArrayList<String> userNames = new ArrayList<>(List.of("Test1", "Test2", "Test3"));
        Random random = new Random();
        List<AWSSpot> allSpots = awsSpots.all();
        System.out.print("[" + Thread.currentThread().getId() + "] simulation in progress\n");
        for (int i = 0; i < iterCount || iterCount == -1; i++) {
            AWSSpot randomSpot = allSpots.get(random.nextInt(allSpots.size()));
            String oldUserName = randomSpot.getUser_name();
            if (random.nextBoolean()) { // reserve
                randomSpot.setMax_price(BigDecimal.valueOf(random.nextFloat(100F)));
                randomSpot.setUser_name(userNames.get(random.nextInt(userNames.size())));
                awsSpotDao.update(randomSpot);
                if (oldUserName.equals(""))
                    spotsReservedDao.increment(randomSpot.getRegion(), randomSpot.getInstance_type(), randomSpot.getAz_name(), 1);
            } else { // release
                AZToEc2MappingDao azToEc2MappingDao = inventoryMapper.azToEc2MappingDao();
                AZToEC2Mapping azToEC2Mapping = azToEc2MappingDao.findByRegionAndInstanceTypeAndAzName(randomSpot.getRegion(), randomSpot.getInstance_type(), randomSpot.getAz_name());
                randomSpot.setMax_price(azToEC2Mapping.getMin_price());
                randomSpot.setUser_name("");
                awsSpotDao.update(randomSpot);
                if (!oldUserName.equals(""))
                    spotsReservedDao.increment(randomSpot.getRegion(), randomSpot.getInstance_type(), randomSpot.getAz_name(), -1);
            }
            if (i % 1000 == 0) {
                String logPath = Thread.currentThread().getId() + "_" + "log_" + i + ".txt";
                File logFile = new File(this.logsDir, logPath);
                try {
                    logFile.createNewFile();
                    FileWriter myWriter = new FileWriter(logFile.getPath());
                    awsSpots = awsSpotDao.findAll();
                    myWriter.write(getSpots((ArrayList<AWSSpot>) awsSpots.all(), false));
                    myWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (i % 100 == 0 && i % 400 != 0 && i != 0) {
                System.out.print(".");
            }
            if (i % 400 == 0 && i != 0) {
                System.out.print("\b\b\b   \b\b\b");
            }
        }
        System.out.println();
    }

    private void printInstanceTypes() {
        Ec2InstanceDao ec2InstanceDao = inventoryMapper.ec2InstanceDao();
        PagingIterable<EC2Instance> ec2Instances = ec2InstanceDao.findAll();
        System.out.println("Instance Type | Instance Family | CPU Cores | Memory Size | Network Performance");
        System.out.println("------------- | --------------- | --------- | ----------- | -------------------");
        ec2Instances.forEach((instance) -> System.out.printf("%13s | %15s | %9d | %11d | %19s\n",
                instance.getInstance_type(),
                instance.getFamily(),
                instance.getVcpu_cores(),
                instance.getMemory_size(),
                instance.getNetwork_performance()));
    }

    private void handleResponse(int response) {
        if (response == 1) {
            optionsMenu();
        } else if (response == 2) {
            showYourReservedSpots();
        } else if (response == 3) {
            printInstanceTypes();
        } else if (response == 4) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the number of Threads ([0, 5]) initialized: ");
            String input;
            int threadNum;
            do {
                 input = scanner.nextLine().trim().strip();
                 try {
                     threadNum = Integer.parseInt(input);
                 } catch (NumberFormatException e) {
                     System.err.println("The given thread number is not a positive integer.");
                     System.err.println("Try again.");
                     continue;
                 }
                 if (threadNum < 0 || threadNum > 5) {
                     System.err.println("The given thread number is not a positive integer.");
                     System.err.println("Try again.");
                     continue;
                 }
                 break;
            }
            while(true);
            System.out.println("Enter number of iterations or -1 for infinite loop: ");
            int iterCount;
            try {
                iterCount = Integer.parseInt(scanner.nextLine().strip());
            } catch (NumberFormatException numberFormatException) {
                System.out.println("Error parsing iteration count. Defaulting to 1000 iterations.");
                iterCount = 1000;
            }

            System.out.println("Simulation started");
            ExecutorService executor = Executors.newFixedThreadPool(threadNum);
            for (int i = 0; i < threadNum; i++) {
                Runnable worker = new AwsConsoleInterface(this.inventoryMapper, iterCount);
                executor.execute(worker);
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                try {
                    if (executor.awaitTermination(1000, TimeUnit.MILLISECONDS))
                        System.out.println("Simulation ended");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (response == 5) {
            handleReleaseSpot();
        }
    }

    private void handleReleaseSpot() {
        ArrayList<AWSSpot> reservedSpots = new ArrayList<>();
        Spliterator<AWSSpot> awsSpotSpliterator = this.inventoryMapper.awsSpotDao().findAll().spliterator();

        awsSpotSpliterator.forEachRemaining((item) -> {
            if (item.getUser_name().equals(this.currUser.getName())) {
                reservedSpots.add(item);
            }
        });
        if (reservedSpots.size() == 0) {
            System.out.println("No reserved spots to release! Exiting.");
        } else {
            releaseSpot(reservedSpots);
        }
    }

    private void showYourReservedSpots() {
        ArrayList<AWSSpot> awsSpots = inventoryMapper.awsSpotDao().findAll().all().stream().filter(
                (awsSpot) ->
                        awsSpot.getUser_name().equals(currUser.getName())
        ).collect(Collectors.toCollection(ArrayList::new));
        printSpots(awsSpots, false);
    }

    private void printSpots(ArrayList<AWSSpot> awsSpots, boolean printNumber) {
        if (printNumber) {
            System.out.print(" Number | ");
        }
        System.out.println("        Region | AZ Name | Instance Type |                              Spot ID | Max Price |      User Name");
        if (printNumber) {
            System.out.print(" ------ | ");
        }
        System.out.println("-------------- | ------- | ------------- | ------------------------------------ | --------- | --------------");
        int i = 0;
        for (AWSSpot awsSpot: awsSpots) {
            if (printNumber) {
                System.out.printf(" %5d. | ", i);
                i++;
            }
            System.out.printf("%14s | %7s | %13s | %36s | %9.4f | %14s\n",
                    awsSpot.getRegion(),
                    awsSpot.getAz_name(),
                    awsSpot.getInstance_type(),
                    awsSpot.getSpot_id().toString(),
                    awsSpot.getMax_price(),
                    awsSpot.getUser_name());
        }
    }

    private String getSpots(ArrayList<AWSSpot> awsSpots, boolean printNumber) {
        StringBuilder logText = new StringBuilder();
        if (printNumber) {
            logText.append(" Number | ");
        }
        logText.append("        Region | AZ Name | Instance Type |                              Spot ID | Max Price |      User Name\n");
        if (printNumber) {
            logText.append(" ------ | ");
        }
        logText.append("-------------- | ------- | ------------- | ------------------------------------ | --------- | --------------\n");
        int i = 0;
        for (AWSSpot awsSpot: awsSpots) {
            if (printNumber) {
                logText.append(String.format(" %5d. | ", i));
                i++;
            }

            logText.append(String.format("%14s | %7s | %13s | %36s | %9.4f | %14s\n",
                    awsSpot.getRegion(),
                    awsSpot.getAz_name(),
                    awsSpot.getInstance_type(),
                    awsSpot.getSpot_id().toString(),
                    awsSpot.getMax_price(),
                    awsSpot.getUser_name()));
        }
        return logText.toString();
    }

    @Override
    public void run() {
        //  System.out.println(Thread.currentThread().getName()+" (Start) message = "+message);
        this.logsDir = new File("logs");
        logsDir.mkdirs();
        startTrafficSimulation(this.iterCount);
    }
}
