import java.util.*;

public class Main {
    private static final String[] gameWords = {"vacation",
            "chocolate",
            "fireworks",
            "manuscript",
            "treasure",
            "jungle",
            "blizzard",
            "orchestra",
            "potion",
            "cathedral"};
    private static final String[] gameHints = {"time off work spent exploring new places",
            "sweet treat made from cacao beans",
            "colorful explosions in the sky on a special occasion",
            "written work submitted for publication",
            "valuable items hidden or buried for discovery",
            "dense forest with diverse plant and animal life",
            "severe snowstorm with strong winds and limited visibility",
            "group of musicians playing various instruments together",
            "liquid with magical or medicinal properties",
            "large and ornate Christian church with a bishop's seat"};
    private static String mainWord = "";
    private static String wordHint = "";
    private static String displayedWord = ""; // word that will be displayed to players
    private static ArrayList<String> usernames = new ArrayList<String>();
    private static ArrayList<Integer> userScores = new ArrayList<Integer>();
    private static ArrayList<Integer> disabledUsers = new ArrayList<Integer>();
    private static final int scoresPerChar = 100;
    private static final String ansiReset = "\u001B[0m";
    private static final String ansiBlue = "\u001B[34m";
    private static final String ansiGreen = "\u001B[32m";
    private static final String ansiYellow = "\u001B[33m";
    private static final String ansiRed = "\u001B[31m";
    private static final String ansiBold = "\033[0;1m";
    private static final String ansiCyan = "\u001B[36m";
    private static final String ansiGrey = "\u001B[37m";

    private static boolean menuIsOn = false;
    private static boolean gameIsStopped = false;
    private static String rank = "Nothing";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(makeColoredWord(ansiCyan, "**WELCOME TO WHEEL OF FORTUNE**") + "\n");

        chooseRandomWord();
        registerUsers();

        System.out.println("Enter " + makeColoredWord(ansiGreen, "/menu") + " to enter game menu");

        int currentUser = 0;
        int winningUser = -1; // doesn't exist yet

        while (true) {
            boolean breakConditions = gameIsStopped || otherUsersAreDisabled() || allCharsGuessed() || userHasAllScores(currentUser);
            if (breakConditions) {
                break;
            }

            if (userIsDisabled(currentUser) || currentUser == winningUser) {
                currentUser = changeUserTurn(currentUser);
                continue;
            }

            showGameInfo(currentUser);
            String userInp = scanner.nextLine();

            if (userInp.equals("/menu") || menuIsOn) {
                cleanScreen();
                doMenuCommand(userInp);
                continue;
            }

            if (!userGuessedChar(userInp, currentUser, winningUser)) {
                currentUser = changeUserTurn(currentUser);
            }

            if (userWillWinAnyway() && winningUser == -1) {
                winningUser = getMostScoreUser();
                rank = makeColoredWord(ansiBlue, "User " + usernames.get(winningUser) + " will win anyway, enter ONLY ENTIRE WORDS");
            }
        }

        cleanScreen();
        showWinner();
        showScores();
    }

    private static void chooseRandomWord() {
        Random random = new Random();
        int randomWordId = random.nextInt(gameWords.length);
        mainWord = gameWords[randomWordId];
        wordHint = gameHints[randomWordId];

        StringBuilder newDisplayedWord = new StringBuilder();
        newDisplayedWord.append("?".repeat(mainWord.length()));
        displayedWord = newDisplayedWord.toString();
    }

    private static void registerUsers() {
        Scanner scanner = new Scanner(System.in);
        int usersCount = 0;
        String errorString = "";
        while (true) {
            System.out.println("Enter your name or " + makeColoredWord(ansiGreen, "/start") + " to start game:");
            if (!errorString.isEmpty()) {
                System.out.println(errorString);
            }
            String username = scanner.nextLine();
            if (username.isEmpty()) {
                errorString = makeColoredWord(ansiRed, "!!!Please enter correct name");
                cleanScreen();
                continue;
            }
            if (username.equals("/start")) {
                if (usersCount < 2) {
                    errorString = makeColoredWord(ansiRed, "!!!Game requires at least 2 players");
                    cleanScreen();
                    continue;
                }
                break;
            }
            if (usernames.contains(username)) {
                errorString = makeColoredWord(ansiRed, "!!!This username already exists, enter other one");
                cleanScreen();
                continue;
            }
            usernames.add(username);
            userScores.add(0);
            errorString = "";
            usersCount++;
            cleanScreen();
        }
        shuffleUsers();
        cleanScreen();
    }

    private static void shuffleUsers () {
        Collections.shuffle(usernames);
    }

    private static boolean userHasAllScores (int currentUser) {
        int userScore = userScores.get(currentUser);
        int allScores = scoresPerChar * mainWord.length();
        return userScore >= allScores;
    }

    private static boolean otherUsersAreDisabled () {
        return disabledUsers.size() >= usernames.size() - 1;
    }

    private static boolean allCharsGuessed () {
        return !displayedWord.contains("?");
    }

    private static boolean userIsDisabled (int currentUser) {
        return disabledUsers.contains(currentUser);
    }

    private static int getMostScoreUser () {
        int winnerId = 0; // by default 1st user
        for (int i = 0; i < usernames.size(); i++) {
            if (userScores.get(i) > userScores.get(winnerId)) {
                winnerId = i;
            }
        }
        return winnerId;
    }

    private static void showWinner () {
        ArrayList<Integer> winners = new ArrayList<Integer>();
        int maximumScore = getMaximumScore();
        for (int i = 0; i < userScores.size(); i++) {
            if (disabledUsers.contains(i)) {
                continue;
            }
            if (userScores.get(i) >= maximumScore) {
                winners.add(i);
                maximumScore = userScores.get(i);
            }
        }

        System.out.print(ansiBlue);
        if (winners.size() > 1) {
            System.out.println("Users " + joinUsers(winners) + " won, tie");
        } else {
            System.out.println("User " + usernames.get(winners.get(0)) + " won");
        }
        System.out.print(ansiReset);
    }

    private static int getMaximumScore () {
        int maximumScore = 0;
        for (int i = 0; i < usernames.size(); i++) {
            if (disabledUsers.contains(i)) {
                continue;
            }
            if (userScores.get(i) > maximumScore) {
                maximumScore = userScores.get(i);
            }
        }
        return maximumScore;
    }

    private static String joinUsers (ArrayList<Integer> array) {
        String resultString = "";
        for (Integer userId : array) {
            if (resultString.isEmpty()) {
                resultString += usernames.get(userId);
                continue;
            }
            resultString += ", " + usernames.get(userId);
        }
        return resultString;
    }

    private static boolean userWillWinAnyway () {
        int allScores = scoresPerChar * mainWord.length();
        for (int i = 0; i < usernames.size(); i++) {
            int userScore = userScores.get(i);

            if (disabledUsers.contains(i)) {
                continue;
            }

            if (userScore > allScores / 2 && userScore < allScores) {
                return true;
            }
        }

        boolean isSecondScoredUserSmallerThanFirst = countRemainingScores() + getSortedScoresArray()[userScores.size() - 2] < getSortedScoresArray()[userScores.size() - 1];
        if (isSecondScoredUserSmallerThanFirst && countRemainingScores() > 0) {
            return true;
        }
        return false;
    }

    private static Integer[] getSortedScoresArray () {
        Integer[] sortedScores = Arrays.copyOf(userScores.toArray(new Integer[0]), userScores.size());
        Arrays.sort(sortedScores);
        return sortedScores;
    }

    private static int countRemainingScores () {
        int allScores = scoresPerChar * mainWord.length();
        int scores = 0;
        for (int i = 0; i < userScores.size(); i++) {
            if (disabledUsers.contains(i)) {
                continue;
            }
            scores+=userScores.get(i);
        }
        return allScores-scores;
    }

    private static void doMenuCommand (String command) {
        showMenu();
        menuIsOn = true;
        switch (command) {
            case "/score":
                showScores();
                break;
            case "/sequence":
                showSequence();
                break;
            case "/menu":
                break;
            case "/stop":
                gameIsStopped = true;
                break;
            case "/continue":
                System.out.println("doing continue...");
                cleanScreen();
                menuIsOn = false;
                break;
            default:
                System.out.println("There is no " + command + " command");
        }
    }

    private static void showMenu () {
        System.out.println("Commands list:");
        System.out.println(makeColoredWord(ansiGreen, "/stop") + " - to stop game");
        System.out.println(makeColoredWord(ansiGreen, "/score") + " - to see scores");
        System.out.println(makeColoredWord(ansiGreen, "/sequence") + " - to see sequence of users");
        System.out.println(makeColoredWord(ansiGreen, "/continue") + " - to continue game");
    }

    private static void showSequence () {
        System.out.print(ansiBlue);
        System.out.println("Sequence:");
        System.out.println(String.join(" => ", usernames));
        System.out.print(ansiReset);
    }

    private static String makeColoredWord (String ansiColor, String word) {
        return ansiColor + word + ansiReset;
    }

    private static void showScores () {
        System.out.print(ansiBlue);
        System.out.println("Scores: ");
        for (int i = 0; i < usernames.size(); i++) {
            if (disabledUsers.contains(i)) {
                System.out.print(ansiGrey);
            }
            System.out.print(usernames.get(i) + ": " + userScores.get(i));
            if (disabledUsers.contains(i)) {
                System.out.print(" (disabled)");
                System.out.print(ansiBlue);
            }
            System.out.println();
        }
        System.out.print(ansiReset);
    }

    private static int changeUserTurn (int currentUser) {
        if (currentUser == usernames.size() - 1) {
            System.out.println("Game: " + usernames.size() + "" + currentUser);
            return 0;
        }
        return currentUser + 1;
    }

    private static void cleanScreen () {
        System.out.println("\033[H\033[2J");
    }

    private static void showGameInfo (int currentUser) {
        if (menuIsOn) {
            return;
        }
        System.out.println("User => " + usernames.get(currentUser));
        System.out.println("Hint => " + wordHint);
        System.out.println("Word =>" + beautifyHiddenWord());
        System.out.println("Info => " + rank);
    }

    private static String beautifyHiddenWord () {
        String innerHiddenWord = "";
        for (int i = 0; i < displayedWord.length(); i++) {
            innerHiddenWord += " | " + makeColoredWord(ansiBold, String.valueOf(displayedWord.charAt(i))).toUpperCase();
        }
        innerHiddenWord += " |";
        return innerHiddenWord;
    }

    private static boolean userGuessedChar(String word, int currentUser, int winningUser) {
        if (word.length() < 1) {
            return false;
        }

        word = word.toLowerCase();
        if (word.length() > 1 || winningUser > -1) {
            guessWord(word, currentUser);
            return true;
        }

        boolean userGuessedCorrectly = false;
        rank = makeColoredWord(ansiYellow, "User " + usernames.get(currentUser)+ " didn't guess char, turn changed");

        StringBuilder newHiddenWord = new StringBuilder();
        for (int i = 0; i < mainWord.length(); i++) {

            if (mainWord.charAt(i) == word.charAt(0) && displayedWord.charAt(i) == '?') {
                newHiddenWord.append(word.charAt(0));
                addScoresForUser(currentUser);
                userGuessedCorrectly = true;
                rank = makeColoredWord(ansiBlue, "User " + usernames.get(currentUser)+ " guessed char");
                continue;
            }
            newHiddenWord.append(displayedWord.charAt(i));
        }

        cleanScreen();
        displayedWord = newHiddenWord.toString();
        return userGuessedCorrectly;
    }

    private static void guessWord(String word, int currentUser) {
        if (mainWord.equals(word)) {
            makeScoresEmpty();
            userScores.set(currentUser, scoresPerChar * mainWord.length());
        } else {
            rank = makeColoredWord(ansiYellow, "User " + usernames.get(currentUser)+ " is disabled");
            disabledUsers.add(currentUser);
        }
    }

    private static void addScoresForUser (int currentUser) {
        int currentScore = userScores.get(currentUser);

        userScores.set(currentUser, scoresPerChar + currentScore);
    }

    private static void makeScoresEmpty () {
        userScores.replaceAll(ignored -> 0);
    }
}