package client;

import com.datastax.oss.driver.api.core.PagingIterable;
import models.entities.AWSSpot;
import models.entities.AZToEC2Mapping;
import models.mappers.InventoryMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Spliterator;

public class AwsConsoleInterface {

    private InventoryMapper inventoryMapper;
    private final String EXIT_CODE = "X";

    public AwsConsoleInterface(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    public void startLoop() {
        this.optionsMenu("# "); //TODO: USUN
        String response;
        printHeader();
        do {
            printMenu();
            response = getResponse();
            handleResponse(response);
            System.out.println();
        } while (!response.equalsIgnoreCase(EXIT_CODE));
    }

    private void printHeader() {
        System.out.println("=========================================================");
        System.out.println("                 AWS Spot Reservator 1.0                 ");
        System.out.println(" Copyright © by Paweł Kryczka and Krzysztof Charlikowski ");
        System.out.println("=========================================================");
        System.out.println();
    }

    private void printMenu() {
        System.out.println("1. Reserve a spot");
        System.out.println("2. Show reserved spots");
        System.out.println("3. Show instance types");
        System.out.println("4. Run big traffic simulation");
        System.out.println(EXIT_CODE + ". Exit");
    }

    private String getResponse() {
        System.out.print("Your choice: ");
        Scanner reader = new Scanner(System.in);
        return reader.nextLine().strip();
    }

    private Object getSelectedItem(ArrayList<Object> selectionList, String loopText) {
        Scanner reader = new Scanner(System.in);
        var resultItem = null;
        boolean indexOk = false;
        do {
            System.out.println(loopText);
            //TODO: reader.nextLine().strip();
            if (indexOk) {
                break;
            }
        } while (true);
    }

    private String optionsMenu(String prompt) {
        PagingIterable<AZToEC2Mapping> azToEC2Mappings = this.inventoryMapper.azToEc2MappingDao().findAll();
        HashSet<String> foundRegions = new HashSet();
        HashSet<String> foundAzs = new HashSet();
        HashSet<String> foundInstanceTypes = new HashSet();
        Spliterator<AZToEC2Mapping> azToEC2MappingSpliterator = azToEC2Mappings.spliterator();
        azToEC2MappingSpliterator.forEachRemaining( (item) -> {
            foundRegions.add(item.getRegion());
            foundAzs.add(item.getAz_name());
            foundInstanceTypes.add(item.getInstance_type());
//            System.out.println(item);
        } );

        System.out.print(prompt);
//        String foundRegionsArr[] = (String[]) foundRegions.toArray();

        String foundAzsList[] = new String[foundAzs.size()];
        foundAzs.toArray(foundAzsList);

        String foundRegionsList[] = new String[foundRegions.size()];
        foundRegions.toArray(foundRegionsList);

        String foundInstanceTypesList[] = new String[foundInstanceTypes.size()];
        foundInstanceTypes.toArray(foundInstanceTypesList);

        int idx = 1;
        for (String region: foundRegionsList) {
            System.out.println(idx + ". " + region);
            idx += 1;
        }

        String chosenRegion = this.getSelectedItem(foundRegionsList, "Choose region");

        idx = 1;
        for (String az: foundAzsList) {
            System.out.println(idx + ". " + az);
            idx += 1;
        }

        idx = 1;
        for (String instanceType: foundInstanceTypesList) {
            System.out.println(idx + ". " + instanceType);
            idx += 1;
        }

        System.out.println("Choose availability zone");
        //
        System.out.println("Choose instance type");
        return "";
    }

    private void handleResponse(String response) {
        System.out.println("Your choice was: " + response);
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
