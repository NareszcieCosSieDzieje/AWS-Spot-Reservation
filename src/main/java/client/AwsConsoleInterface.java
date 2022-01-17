package client;

import client.security.SecurePassword;
import com.datastax.oss.driver.api.core.PagingIterable;
import models.daos.AWSSpotDao;
import models.entities.AWSSpot;
import models.entities.AZToEC2Mapping;
import models.entities.User;
import models.mappers.InventoryMapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.stream.Collectors;

public class AwsConsoleInterface {

    private InventoryMapper inventoryMapper;
    private final int EXIT_CODE = 0;
    private User currUser = null;

    public AwsConsoleInterface(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
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
        System.out.println("1. Reserve a spot"); // TODO: Update spots reserved table
        System.out.println("2. Show reserved spots");
        System.out.println("3. Show instance types");
        System.out.println("4. Run big traffic simulation");
        System.out.println("5. Release reserved spot"); // TODO: Handle this
        System.out.println(EXIT_CODE + ". Exit");
    }

    private boolean handleUserLogin() {
        System.out.print("Enter username: ");
        Scanner reader = new Scanner(System.in);
        String username = reader.nextLine().strip();

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
            System.err.println("Choice invalid. Choice range [0, 4]");
        }
        if (choice < 0 || choice > 4) {
            System.err.println("Choice invalid. Choice range [0, 4]");
        }
        return choice;
    }

    private <T> T getSelectedItem(ArrayList<T> selectionList, String loopText) {
        Scanner reader = new Scanner(System.in);
        int indexStart = 0;
        int indexEnd = selectionList.size() - 1;
        int chosenIndex = -1;
        final Runnable errorMessage = () -> {
            System.out.println("Index must be a valid integer! The accepted range is [" + indexStart + ", " + indexEnd + "]");
        };

        // TODO: leaving the function with 'X' or smth
        do {
            System.out.println(loopText);
            try {
                chosenIndex = Integer.parseInt(reader.nextLine().strip());
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

    private void optionsMenu(String prompt) {
        PagingIterable<AZToEC2Mapping> azToEC2Mappings = this.inventoryMapper.azToEc2MappingDao().findAll();
        HashSet<String> foundRegionsSet = new HashSet();
        HashSet<String> foundAzsSet = new HashSet();
        HashSet<String> foundInstanceTypesSet = new HashSet();
        Spliterator<AZToEC2Mapping> azToEC2MappingSpliterator = azToEC2Mappings.spliterator();
        azToEC2MappingSpliterator.forEachRemaining( (item) -> {
            foundRegionsSet.add(item.getRegion());
            foundAzsSet.add(item.getAz_name());
            foundInstanceTypesSet.add(item.getInstance_type());
//            System.out.println(item);
        } );

//        System.out.print(prompt);

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

        for(List<String> list: listOfLists) {
            int listIndex = listOfLists.indexOf(list);
            if (listIndex < 0) {
                System.err.println("Error getting the list index!");
            }
            int idx = 0;
            if (list.size() == 0) {
                System.out.println("List of: " + listIndexToKeyMapping.get(listIndex) + " is empty.");
                continue;
            }
            for (String elem: list) {
                System.out.println(idx + ". " + elem);
                idx += 1;
            }
            String chosenItem = this.getSelectedItem(foundRegionsList, listPromptMap.get(listIndex));
            String chosenElemKey = listIndexToKeyMapping.get(listIndex);
            chosenElements.put(chosenElemKey, chosenItem);
        }

        System.out.println("Chose the maximum price you are willing to pay for the Spot");
        System.out.println("Your credits: " + this.currUser.getCredits());
        BigDecimal minPrice = new BigDecimal(0); // FIXME WHAT IS THE MIN PRICE!
        BigDecimal maxPrice = this.currUser.getCredits(); // fixme czy to jest max czy jeszcze cos
        Scanner scanner = new Scanner(System.in);
        BigDecimal chosenMaxPrice = null;
        do {
            System.out.println("");
            try {
                chosenMaxPrice = new BigDecimal(scanner.nextLine().strip());
            } catch (NumberFormatException e) {
                System.out.println("Provided number was not valid.");
                System.out.println("Accepting values from range: [" + minPrice + ", " + maxPrice + "}");
                continue;
            }
            if (chosenMaxPrice.compareTo(minPrice) == -1 || chosenMaxPrice.compareTo(maxPrice) == 1) {
                System.out.println("Provided number was not valid.");
                System.out.println("Accepting values from range: [" + minPrice + ", " + maxPrice + "}");
                continue;
            }
            break;
        } while (true);

        // FIXME: walidacja chosenElements zeby nie bylo pustych stringow?

        AWSSpot awsSpot = new AWSSpot(  chosenElements.get("region"),
                                        chosenElements.get("az_name"),
                                        chosenElements.get("instance_type"),
                chosenMaxPrice,
                                        this.currUser.getName());

        this.inventoryMapper.awsSpotDao().save(awsSpot);
    }

    private void handleResponse(int response) {
        System.out.println("Your choice was: " + response);

        // TODO: GET USER AND ID
        if (response == 1) {
            /* TODO:
            * PRINT SPOTS with stats?
            * again get choice?
            * reserve the spot, or multiple?
            * */
            optionsMenu("Reserving a new spot...");
        } else if (response == 2) {
            // show spots belonging to the user
            ArrayList<AWSSpot> awsSpots = inventoryMapper.awsSpotDao().findAll().all().stream().filter(
                    (awsSpot) ->
                            awsSpot.getUser_name().equals(currUser.getName())
            ).collect(Collectors.toCollection(ArrayList::new));
            // TODO: PRINT SPOTS BELONGING TO USER
            System.out.printf("Reserved spots: \n");
            for(int i = 0; i < awsSpots.size(); i++) {
                System.out.printf(i + ". " + awsSpots.get(i)); //FIXME przeskalowac o 1+?
            }
        } else if (response == 3) {
            // TODO: print instance types?
        } else if (response == 4) {
            // TODO: simulation?
        }
    }

    private void reserveSpot(String region, String instance_type, String az_name) {
        AZToEC2Mapping azToEC2Mappings = this.inventoryMapper.azToEc2MappingDao().
                findByRegionAndInstanceTypeAndAzName(region, instance_type, az_name);
    }

    private void showSpots() {
        PagingIterable<AWSSpot> awsSpots = this.inventoryMapper.awsSpotDao().findAll();
        for (AWSSpot awsSpot: awsSpots) {
            System.out.println("spot: " + awsSpot.toString());
        }
    }

}
