package co.istad;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String NUMERIC_PATTERN = "\\d+";
    private static int bookingNumber = 1;
    private static int totalRows;
    private static int seatsPerRow;
    private static boolean[][] morningSeatsAvailability;
    private static boolean[][] afternoonSeatsAvailability;
    private static boolean[][] nightSeatsAvailability;
    private static String[][] bookingHistory;
    private static int bookingCount;
    private static int overallBookingCount = 1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("-+-+-+-+-+- CSTAD HALL BOOKING SYSTEM -+-+-+-+-+-+");

        // Configure total rows and seats per row
        System.out.print("Enter total rows in the hall: ");
        String totalRowsInput = scanner.next();
        while (!totalRowsInput.matches(NUMERIC_PATTERN)) {
            System.out.println("Invalid input. Please enter a numeric value for total rows.");
            System.out.print("Enter total rows in the hall: ");
            totalRowsInput = scanner.next();
        }
        totalRows = Integer.parseInt(totalRowsInput);

        System.out.print("Enter total seats per row in the hall: ");
        String seatsPerRowInput = scanner.next();
        while (!seatsPerRowInput.matches(NUMERIC_PATTERN)) {
            System.out.println("Invalid input. Please enter a numeric value for total seats per row.");
            System.out.print("Enter total seats per row in the hall: ");
            seatsPerRowInput = scanner.next();
        }
        seatsPerRow = Integer.parseInt(seatsPerRowInput);

        // Initialize seatsAvailability based on user input
        morningSeatsAvailability = new boolean[totalRows][seatsPerRow];
        afternoonSeatsAvailability = new boolean[totalRows][seatsPerRow];
        nightSeatsAvailability = new boolean[totalRows][seatsPerRow];
        bookingHistory = new String[100][5]; // Assuming max 100 bookings

        while (true) {
            displayMenu();
            System.out.print("Please select menu no: ");
            String choice = scanner.next().toUpperCase();
            switch (choice) {
                case "A":
                    System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
                    System.out.println("Start Booking Process");

                    // Display the Showtime schedule
                    displayShowtimeSchedule();
                    System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
                    // Display the hall name based on the selected showtime
                    System.out.print("Please select show time (A | B | C): ");
                    String showtimeChoice = scanner.next().toUpperCase();

                    // Check if it's a valid showtime
                    if (!isValidShowtimeChoice(showtimeChoice)) {
                        System.out.println("Invalid showtime choice.");
                        continue;
                    }

                    displayHallName(showtimeChoice);
                    // Display available rows and columns
                    displayAvailableSeats(showtimeChoice);

                    System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");

                    // Allow the user to select seat(s)
                    System.out.println("# INSTRUCTION");
                    System.out.println("# Single seat: A-1");
                    System.out.println("# Multiple seats (separate by comma): A-1, A-2");
                    System.out.print("> Please select available seat: ");
                    scanner.nextLine();  // Consume the newline character
                    String seatChoice = scanner.nextLine().toUpperCase();

                    if (isValidSeatChoice(seatChoice) && areSeatsAvailable(seatChoice.split(","), showtimeChoice)) {
                        // Ask for student ID
                        System.out.print("> Please enter student ID: ");
                        String studentID = scanner.nextLine();
                        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");

                        // Confirm booking
                        System.out.print("> Are you sure to book? (Y/N): ");
                        String confirmChoice = scanner.next().toUpperCase();
                        if (confirmChoice.equals("Y") || confirmChoice.equals("YES")) {
                            bookSeats(seatChoice, showtimeChoice, studentID);
                            System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
                            // Display the success message
                            displayBookingConfirmation(seatChoice.split(","));
                        } else {
                            System.out.println("--------------------------------------------------");
                            System.out.println(" ".repeat(15) + "Booking Canceled!" + " ".repeat(15));
                            System.out.println("--------------------------------------------------");
                        }
                    } else {
                        System.out.println("Invalid seat choice or some seats are not available. Booking failed!");
                    }
                    break;

                case "B":
                    displayHallSeats();
                    break;

                case "C":
                    displayShowtimeSchedule();
                    break;

                case "D":
                    displayBookingHistory();
                    break;

                case "E":
                    rebootHall();
                    break;

                case "F":
                    System.out.println("Exiting the Hall Booking System. Goodbye!");
                    System.exit(0);

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
        System.out.println("[[ Application Menu ]]");
        System.out.println("<A> Book a Hall");
        System.out.println("<B> Display Hall Seats");
        System.out.println("<C> Display Showtime Schedule");
        System.out.println("<D> Display Booking History");
        System.out.println("<E> Reboot Hall");
        System.out.println("<F> Exit");
    }

    private static void displayShowtimeSchedule() {
        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
        System.out.println("# Daily Showtime of CSTAD Hall:");
        System.out.println("# A) Morning (10:00AM - 12:30PM)");
        System.out.println("# B) Afternoon (03:00PM - 05:30PM)");
        System.out.println("# C) Night (07:00PM - 09:30PM)");
    }

    private static void displayHallName(String showtimeChoice) {
        String showtimeLabel = getShowtimeLabel(showtimeChoice);
        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
        System.out.println("# Hall - " + showtimeLabel);
    }

    private static String getShowtimeLabel(String showtimeChoice) {
        switch (showtimeChoice) {
            case "A":
                return "Morning";
            case "B":
                return "Afternoon";
            case "C":
                return "Night";
            default:
                return "Invalid showtime choice";
        }
    }

    private static void displayAvailableSeats(String showtimeChoice) {
        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < seatsPerRow; j++) {
                char rowLabel = (char) ('A' + i);
                String seatStatus = getSeatStatus(i, j, showtimeChoice);
                System.out.print("|" + rowLabel + "-" + (j + 1) + "::" + seatStatus + "| ");
            }
            System.out.println();
        }
    }

    private static String getSeatStatus(int rowIndex, int colIndex, String showtimeChoice) {
        switch (showtimeChoice) {
            case "A":
                return morningSeatsAvailability[rowIndex][colIndex] ? "BO" : "AV";
            case "B":
                return afternoonSeatsAvailability[rowIndex][colIndex] ? "BO" : "AV";
            case "C":
                return nightSeatsAvailability[rowIndex][colIndex] ? "BO" : "AV";
            default:
                return "Invalid showtime choice";
        }
    }

    private static boolean isValidSeatChoice(String seatChoice) {
        String[] seats = seatChoice.split(",");
        for (String seat : seats) {
            Pattern pattern = Pattern.compile("[A-" + (char) ('A' + totalRows - 1) + "]-[1-" + seatsPerRow + "]");
            Matcher matcher = pattern.matcher(seat.trim());
            if (!matcher.matches()) {
                return false;
            }
        }
        return true;
    }

    private static boolean areSeatsAvailable(String[] selectedSeats, String showtimeChoice) {
        String[] unavailableSeats = new String[selectedSeats.length];
        int unavailableCount = 0;

        for (String seat : selectedSeats) {
            if (!isSeatAvailable(seat.trim(), showtimeChoice)) {
                unavailableSeats[unavailableCount++] = seat.trim();
            }
        }

        if (unavailableCount > 0) {
            displayUnavailableSeatsMessage(Arrays.copyOf(unavailableSeats, unavailableCount));
            return false;
        }

        return true;
    }

    private static void displayUnavailableSeatsMessage(String[] unavailableSeats) {
        System.out.print("!! ");
        if (unavailableSeats.length == 1) {
            System.out.println("[" + unavailableSeats[0] + "] is already booked!");
        } else {
            System.out.print("[");
            for (int i = 0; i < unavailableSeats.length; i++) {
                if (i > 0) {
                    System.out.print(" and ");
                }
                System.out.print(unavailableSeats[i]);
            }
            System.out.println("] are already booked!");
            System.out.print("!! [");
            for (int i = 0; i < unavailableSeats.length; i++) {
                if (i > 0) {
                    System.out.print(" and ");
                }
                System.out.print(unavailableSeats[i]);
            }
            System.out.println("] can't be booked because of unavailability!");
        }
    }

    private static boolean isSeatAvailable(String seatChoice, String showtimeChoice) {
        String[] selectedSeats = seatChoice.split(",");
        for (String seat : selectedSeats) {
            String trimmedSeat = seat.trim();
            char rowLabel = trimmedSeat.charAt(0);
            int colNumber = Integer.parseInt(trimmedSeat.substring(2));
            int rowIndex = rowLabel - 'A';

            if (rowIndex < 0 || rowIndex >= totalRows || colNumber < 1 || colNumber > seatsPerRow ||
                    (getSeatStatus(rowIndex, colNumber - 1, showtimeChoice).equals("BO"))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidShowtimeChoice(String showtimeChoice) {
        return showtimeChoice.matches("[A-C]");
    }

    private static void bookSeats(String seatChoice, String showtimeChoice, String studentID) {
        String[] selectedSeats = seatChoice.split(",");

        for (String seat : selectedSeats) {
            bookSingleSeat(seat.trim(), showtimeChoice, studentID);
        }

        // Increment overall booking count here, after all seats are booked
        overallBookingCount++;
    }

    private static void bookSingleSeat(String selectedSeat, String showtimeChoice, String studentID) {
        char rowLabel = selectedSeat.charAt(0);
        int colNumber = Integer.parseInt(selectedSeat.substring(2));

        // Book the hall
        int rowIndex = rowLabel - 'A';
        boolean[][] seatsAvailability;

        switch (showtimeChoice) {
            case "A":
                seatsAvailability = morningSeatsAvailability;
                break;
            case "B":
                seatsAvailability = afternoonSeatsAvailability;
                break;
            case "C":
                seatsAvailability = nightSeatsAvailability;
                break;
            default:
                System.out.println("Invalid showtime choice.");
                return;
        }

        seatsAvailability[rowIndex][colNumber - 1] = true;

        bookingHistory[bookingCount][0] = String.valueOf(rowLabel);
        bookingHistory[bookingCount][1] = String.valueOf(colNumber);
        bookingHistory[bookingCount][2] = studentID;
        bookingHistory[bookingCount][3] = showtimeChoice;
        bookingHistory[bookingCount][4] = String.valueOf(overallBookingCount);  // Assign overallBookingCount directly
        bookingCount++;
    }

    private static void displayBookingConfirmation(String[] selectedSeats) {
        System.out.print("# [");
        for (int i = 0; i < selectedSeats.length; i++) {
            if (i > 0) {
                System.out.print(" and ");
            }
            System.out.print(selectedSeats[i]);
        }
        System.out.println("] booked successfully!");
    }

    private static void displayHallSeats() {
        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-++-+-+-+-+-+-+-+-+");
        System.out.println("# Hall Information");
        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-++-+-+-+-+-+-+-+-+");

        System.out.println("# Hall - Morning");
        displaySeats(morningSeatsAvailability);
        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-++-+-+-+-+-+-+-+-+");

        System.out.println("# Hall - Afternoon");
        displaySeats(afternoonSeatsAvailability);

        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-++-+-+-+-+-+-+-+-+");
        System.out.println("# Hall - Night");
        displaySeats(nightSeatsAvailability);
    }

    private static void displaySeats(boolean[][] seatsAvailability) {
        for (int i = 0; i < totalRows; i++) {
            char rowLabel = (char) ('A' + i);
            System.out.print("|" + rowLabel + "-1::" + (seatsAvailability[i][0] ? "BO" : "AV") + "| ");
            for (int j = 1; j < seatsPerRow; j++) {
                System.out.print("|" + rowLabel + "-" + (j + 1) + "::" + (seatsAvailability[i][j] ? "BO" : "AV") + "| ");
            }
            System.out.println();
        }
    }

    private static void displayBookingHistory() {
        System.out.println("-+-+-+-+-+-+-+-+- Booking History -+-+-+-+-+-+-+-+-+-");

        if (bookingCount == 0) {
            System.out.println("-----------------------------------------------------");
            System.out.println(" ".repeat(15) + "There is no history!" + " ".repeat(15));
            System.out.println("-----------------------------------------------------");
        } else {
            Arrays.sort(bookingHistory, 0, bookingCount, Comparator.comparing(booking -> Integer.parseInt(booking[4])));
            for (int i = 0; i < bookingCount; i++) {
                if (i > 0) {
                    System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-");
                }

                System.out.println("#NO: " + bookingHistory[i][4]);
                int j = i;
                System.out.print("#SEATS: [");

                // Display multiple seats in the same booking
                while (j < bookingCount && bookingHistory[j][0].equals(bookingHistory[i][0])
                        && bookingHistory[j][2].equals(bookingHistory[i][2])
                        && bookingHistory[j][3].equals(bookingHistory[i][3])) {
                    if (j > i) {
                        System.out.print(", ");
                    }
                    System.out.print(bookingHistory[j][0] + "-" + bookingHistory[j][1]);
                    j++;
                }

                System.out.println("]");
                System.out.println("#HALL: " + getHallName(bookingHistory[i][3]));
                System.out.println("#STU.ID: " + convertToUUID(bookingHistory[i][2]));
                System.out.println("#Created Date: " + getCurrentDate());
                i = j - 1;
            }
        }
    }

    private static String convertToUUID(String numericID) {
        // Convert numeric ID to UUID
        long numericValue = Long.parseLong(numericID);
        UUID uuid = new UUID(0, numericValue);
        return uuid.toString();
    }

    private static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private static String getHallName(String showtimeChoice) {
        switch (showtimeChoice) {
            case "A":
                return "Hall Morning";
            case "B":
                return "Hall Afternoon";
            case "C":
                return "Hall Night";
            default:
                return "Invalid showtime choice";
        }
    }

    private static void rebootHall() {
        System.out.println("-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
        // Reboot the hall (reset seat availability)
        for (int i = 0; i < totalRows; i++) {
            for (int j = 0; j < seatsPerRow; j++) {
                morningSeatsAvailability[i][j] = false;
                afternoonSeatsAvailability[i][j] = false;
                nightSeatsAvailability[i][j] = false;
            }
        }

        // Reset booking history
        bookingHistory = new String[100][5];

        // Reset booking count
        bookingCount = 0;

        // Reset overall booking count to 1
        overallBookingCount = 1;

        System.out.println("Start rebooted the hall...");
        System.out.println("Hall rebooted successfully!");
    }
}
