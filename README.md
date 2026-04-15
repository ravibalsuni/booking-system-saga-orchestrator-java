# booking-system-saga-orchestrator-java
A booking system with services for reservations, payments, and confirmations, let's design using Kafka and a booking orchestrator. A Saga Orchestrator Pattern

# refer to the linkedIn post article her 
https://www.linkedin.com/pulse/design-architecture-perspective-orchestrated-saga-pattern-balsuni-nzyhf/

The booking-orchestrator is the central coordinator that drives the multi-step booking flow through a defined state machine: 

create reservation → process payment → generate confirmation. 
It sends command events to downstream services and reacts to their reply events to advance or compensate the saga.

On failure, it triggers compensating actions to undo previously completed steps - keeping the distributed system consistent without requiring distributed transactions.

In a traditional distributed transaction (like two-phase commit / 2PC), you'd have a single transaction coordinator that locks resources across all participating services and either commits or rolls back everything atomically. That gives you ACID guarantees across service boundaries, but it's slow, tightly coupled, and doesn't scale well - each service has to hold locks and wait for the coordinator, and if any participant is down, the whole transaction blocks.

The saga pattern sidesteps all of that. Each service runs its own local transaction independently against its own database. There's no cross-service locking or shared transaction context. Instead of a global rollback, the orchestrator issues compensating commands to undo work that already committed.


# Step 1  Install any kafka version (refer steps below if needed)
kafka_2.12-2.8.0 repo (This demo was implemented on ubuntu 18.04)

	Installing Kafka 2.12-2.8.0

	1.1) download binaries - 'kafka_2.12-2.8.0.tgz' from:
	     https://github.com/ravibalsuni/springboot-java-kafka/blob/master/kafka_2.12-2.8.0.tgz

	1.2) Unzip the folder you downloaded in the required installation path as per your choice. 
	     The contents extract to a folder named "kafka_2.12-2.8.0"
	     e.g. /home/ravi/Downloads/kafka_2.12-2.8.0.tgz

	1.3) my installation directory for reference -  /home/ravi/kafka-installation/kafka_2.12-2.8.0

	1.4) create following scripts to start Zookeeper first and then Kafka
	     start-kafka.sh		https://github.com/ravibalsuni/springboot-java-kafka/start-kafka.sh
	     stop-kafka.sh		https://github.com/ravibalsuni/springboot-java-kafka/stop-kafka.sh
	     start-zookeeper.sh		https://github.com/ravibalsuni/springboot-java-kafka/start-zookeeper.sh
	     stop-zookeeper.sh		https://github.com/ravibalsuni/springboot-java-kafka/stop-zookeeper.sh

	1.5) modify these script files with your installation path as per point 1.3) 
	     i.e. replace line 1 of all the script files
	     	cd /home/ravi/kafka-installation/kafka_2.12-2.8.0/
	     as per your installation path

	1.6) run following command to start zookeeper server first (use your installation path as per 1.3) above )
		 
		 /home/ravi/kafka-installation/start-zookeeper.sh
		
		 or
		 
		 cd  /home/ravi/kafka-installation
		 ./start-zookeeper.sh

		# Note - Zookeeper user following port by default "zookeeper.connect=localhost:2181" and "clientPort=2181"
		  located at -
		   ( 
			/home/ravi/kafka-installation/kafka_2.12-2.8.0/config/server.properties
				and
			/home/ravi/kafka-installation/kafka_2.12-2.8.0/config/zookeeper.properties
		   )

		# Error
			
			if the server is not running up try to change the port number in above files
			or you can use 	following command - sudo kill -9 `sudo lsof -t -i:2181`and 
			re-try this step 1.6) again by running start-zookeeper.sh script
	
	1.7) similarly run following command to start kafka server later

		 /home/ravi/kafka-installation/start-kafka.sh

		 or

		 cd  /home/ravi/kafka-installation
		 ./start-kafka.sh
		 
# Step 2
	run all the spring boot projects (booking-orchestrator, reservation-service, payment-service, confirmation-service )
	
# Step 3

Sample requests

## 1. Create Booking

Request:

{
    "date": "2026-05-20",
    "time": "19:30",
    "guestName": "John Doe",
    "guestEmail": "john.doe@example.com",
    "amount": 150.00,
    "paymentMethod": "CREDIT_CARD"
}
  

Response(202 Accepted):

{
  "sagaId": "4798a501-9bb4-48de-8925-15db1daf7329",
  "reservationId": "cb73c5d1-a109-44f5-b1bd-10a1dcf497b9",
  "state": "STARTED",
  "traceId": "5753774b-cb8a-4679-97d6-7d0bd8db13c5"
}

## 2. Check Booking Status

Get Request:

http://localhost:8080/api/bookings/4798a501-9bb4-48de-8925-15db1daf7329/status

Response: 

{
  "sagaId": "4798a501-9bb4-48de-8925-15db1daf7329",
  "state": "CONFIRMED",
  "failureReason": null,
  "createdAt": "2026-04-14T17:07:20.509710Z",
  "updatedAt": "2026-04-14T17:07:20.767968Z"
}


## 3. Get Full Booking Details

Request:

http://localhost:8080/api/bookings/4798a501-9bb4-48de-8925-15db1daf7329

Response:

{
  "id": "4798a501-9bb4-48de-8925-15db1daf7329",
  "reservationId": "cb73c5d1-a109-44f5-b1bd-10a1dcf497b9",
  "paymentId": "14f0b92b-b47c-4c01-ad1c-4ba6f1748c92",
  "confirmationNumber": "CONF-C1BF1F02",
  "state": "CONFIRMED",
  "failureReason": null,
  "traceId": "5753774b-cb8a-4679-97d6-7d0bd8db13c5",
  "date": "2026-05-20",
  "time": "19:30:00",
  "guestName": "John Doe",
  "guestEmail": "john.doe@example.com",
  "amount": 150.00,
  "paymentMethod": "CREDIT_CARD",
  "createdAt": "2026-04-14T17:07:20.509710Z",
  "updatedAt": "2026-04-14T17:07:20.767968Z"
}

## 4. Get Confirmation

Request:

 http://localhost:8080/api/bookings/4798a501-9bb4-48de-8925-15db1daf7329/confirmation
 
 
Response:

{
  "sagaId": "4798a501-9bb4-48de-8925-15db1daf7329",
  "reservationId": "cb73c5d1-a109-44f5-b1bd-10a1dcf497b9",
  "confirmationNumber": "CONF-C1BF1F02",
  "guestName": "John Doe",
  "guestEmail": "john.doe@example.com",
  "date": "2026-05-20",
  "time": "19:30:00"
}


## 5. Get Bookings by Email

Request:

 http://localhost:8080/api/bookings?email=john.doe@example.com
 

Response:

  {
    "id": "4798a501-9bb4-48de-8925-15db1daf7329",
    "reservationId": "cb73c5d1-a109-44f5-b1bd-10a1dcf497b9",
    "paymentId": "14f0b92b-b47c-4c01-ad1c-4ba6f1748c92",
    "confirmationNumber": "CONF-C1BF1F02",
    "state": "CONFIRMED",
    "guestName": "John Doe",
    "guestEmail": "john.doe@example.com",
	"amount": 150.00,
    "paymentMethod": "CREDIT_CARD"
  }


## 6. Cancel Booking (Compensation Flow)

Request:

curl -X DELETE http://localhost:8080/api/bookings/4798a501-9bb4-48de-8925-15db1daf7329

Response:

{
  "message": "Cancellation initiated",
  "sagaId": "4798a501-9bb4-48de-8925-15db1daf7329"
}


**Status after cancellation:**

{
  "sagaId": "4798a501-9bb4-48de-8925-15db1daf7329",
  "state": "COMPENSATED",
  "failureReason": null,
  "createdAt": "2026-04-14T17:07:20.509710Z",
  "updatedAt": "2026-04-14T17:08:15.528799Z"
}

Compensation flow: CONFIRMED → COMPENSATING → COMPENSATED (refund + reservation cancelled)

## Saga State Transitions

### Happy Path

STARTED → RESERVATION_CREATED → PAYMENT_COMPLETED → CONFIRMED

### Cancellation Path

CONFIRMED → COMPENSATING → COMPENSATED
