## AWS-SPOT-RESERVATIONS

This app is supposed to simulate  
resource allocation in a distributed  
cloud environment where users can outbid  
each-other to gain resources in the  
form of AWS-Spot instances.

## Technology:
- JAVA 
- MAVEN
- CASSANDRA

## Database schema:
Table AWSSpot
* region text, PK FK -> AvailabilityZone
* az_name text, PK FK -> AvailabilityZone
* instance_type ascii PK FK -> EC2Instance(instance_type)
* max_price decimal, CC # how much are you willing to pay to keep running instance

Table EC2Instance
* instance_type ascii, PK # t2.micro, t2.small
* family ascii, CC # t2
* vcpu_cores int,
* memory_size int,
* network_performance text

Table AZToEC2Mapping
* region text, FK -> AvailabilityZone PK
* instance_type ascii, FK -> EC2Instance PK
* az_name text, FK -> AvailabilityZone CC
* min_price decimal, # CONSTANT
* current_price decimal, # BASED ON spots_reserved and instance type
* max_spots_available int,
* spots_reserved counter

Table SpotsReserved
* region text, PK
* instance_type ascii, PK
* az_name text PK
* spots_reserved counter

Table AvailabilityZone
* region text, PK # US EAST
* name text, CC # 1a
* status text # up, down