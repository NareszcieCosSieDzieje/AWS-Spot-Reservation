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
        System.out.println("1. Reserve a spot"); // TODO: Update spots reserved table, check aztoec2mapping and dont allow to create spot if max_spots exceeded
        System.out.println("2. Show reserved spots");
        System.out.println("3. Show instance types");
        System.out.println("4. Run big traffic simulation");
        System.out.println("5. Release reserved spot"); // TODO: Handle this, delete spot, decrease counter
        System.out.println(EXIT_CODE + ". Exit");
    }

    private boolean handleUserLogin() {
        String username = null;
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
        System.out.println("Press 'X' to exit");
        do {
            System.out.println(loopText);
            try {
                String input = reader.nextLine().strip();
                if (input.equals("X")) {
                    return null;
                }
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
        ArrayList<AWSSpot> availableAWSSpotsForThePrice = new ArrayList<>();
        BigDecimal chosenMaxPrice = null;
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

            for (List<String> list : listOfLists) {
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
                String chosenItem = this.getSelectedItem(foundRegionsList, listPromptMap.get(listIndex));
                String chosenElemKey = listIndexToKeyMapping.get(listIndex);
                if (chosenItem == null) {
                    chosenElements.put(chosenElemKey, "");
                } else {
                    chosenElements.put(chosenElemKey, chosenItem);
                }
            }

            System.out.println("Chose the maximum price you are willing to pay for the Spot");
            System.out.println("Your credits: " + this.currUser.getCredits());
            // FIXME CO JAK JAKIS PARAMETR JEST PUSTY?

            AZToEC2Mapping azToEC2Mapping = this.inventoryMapper.azToEc2MappingDao().findByRegionAndInstanceTypeAndAzName(
                    chosenElements.get("region"),
                    chosenElements.get("az_name"),
                    chosenElements.get("instance_type"));
            BigDecimal minPrice = new BigDecimal("0.2"); // FIXME! ?
            if (azToEC2Mapping != null) {
                minPrice = azToEC2Mapping.getMin_price();
            }

            BigDecimal maxPrice = this.currUser.getCredits(); // TODO: czy JEST Git taki max
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
                if (chosenMaxPrice.compareTo(minPrice) == -1 || chosenMaxPrice.compareTo(maxPrice) == 1) {
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
                        item.getMax_price().compareTo(finalChosenMaxPrice) == -1) {
                    availableAWSSpotsForThePrice.add(item);
                }
            });

            availableAWSSpotsForThePrice.sort(AWSSpot.sortByMaxPrice);
            // TODO: DEBUG

            boolean startOver = false;
            if (availableAWSSpotsForThePrice.size() == 0) {
                System.out.println("No AWSSpots available for the price: " + chosenMaxPrice);
                Scanner reader = new Scanner(System.in);
                do {
                    System.out.println("Press enter to start over.");
                    System.out.println("Press 'X' to exit.");
                    String input = reader.nextLine().strip();
                    if (input.isBlank()) {
                        System.out.println("Starting over.\n");
                        startOver = true;
                        break;
                    } else if (input.equals("X")) {
                        System.out.println("Exiting.");
                        return;
                    }
                } while (true);
                if (startOver) {
                    continue;
                }
            }
            break;
        } while (true);

        AWSSpot chosenSpot = availableAWSSpotsForThePrice.get(0);
        chosenSpot.setMax_price(chosenMaxPrice);
        chosenSpot.setUser_name(this.currUser.getName());
        this.currUser.setCredits(this.currUser.getCredits().subtract(chosenMaxPrice));

        this.inventoryMapper.userDao().update(this.currUser);
        this.inventoryMapper.awsSpotDao().update(chosenSpot);
    }

    private void releaseSpot(ArrayList<AWSSpot> reservedSpots ) {
        // TODO: PRINT ALL RESERVED SPOTS
        Scanner scanner = new Scanner(System.in);
        int maxIndex = reservedSpots.size() - 1;
        int chosenSpotID = -1;
        do {
            // TODO: print spots by number
            System.out.println("Chose your number for the spot to be released");
            System.out.println("Press 'X' to exit");
            try {
                String input = scanner.nextLine().strip();
                if (input.equals("X")) {
                    System.out.println("Exiting.");
                    return;
                }
                chosenSpotID = Integer.parseInt(scanner.nextLine().strip());
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
        spotToBeReleased.setMax_price(new BigDecimal("0.2f")); //FIXME CZY TO OK?
        this.inventoryMapper.awsSpotDao().update(spotToBeReleased);
        System.out.println("Successfully released spot no.: " + chosenSpotID);
        // TODO: PRINT ALL RESERVED SPOTS
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
            System.out.printf("Reserved spots: \n");
            for(int i = 0; i < awsSpots.size(); i++) {
                System.out.printf(i + ". " + awsSpots.get(i) + "\n");
            }
        } else if (response == 3) {
            // TODO: print instance types? kindof DONE get code from options menu
        } else if (response == 4) {
            // TODO: simulation? IMPORTANT
            // TODO: if spot_count = max then cheapest one changes user_name and max_price
        } else if (response == 5) {
            // TODO: Handle this, delete spot, decrease counter
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
