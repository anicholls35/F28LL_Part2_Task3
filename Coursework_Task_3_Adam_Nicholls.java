import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Below holds the all the code for F28LL Part 2 Task 3 Coursework. The main method is the starting point
 * for the application to run. To meet the standards of the coursework (single .java file, all support classes
 * have been made static - it should be noted that this isn't standard practice for a Java application and has just
 * been done for the purpose of this coursework. A Brief explanation of what the Static keyword does in Java will
 * be provided in my video explanation and demo of the code.
 *
 * <br><br>
 * Dummy Data Sourced from:
 * <a href="https://www.kaggle.com/datasets/prasad22/daily-transactions-dataset?resource=download">
 *     CSV Daily Transactions Dataset
 * </a>
 * <br><br>
 * <h3><i>
 *     NOTE: Dataset used for this coursework was sourced via a 3rd party due to the nature of my company;
 *     real data can't be used - even from test servers.
 * </i></h3>
 *
 * @author Adam Nicholls
 * @version 1
 * @since 08.07.2024
 */
class F28LLPart2Task3 {
    public static void main(String[] args) {
        CsvReader reader = new CsvReader();

        reader.read("Daily Household Transactions.csv").stream()
                .map(Transaction::fromMap)
                .filter(transaction -> {
                    try {
                        Field[] fields = transaction.getClass().getDeclaredFields();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            if (field.get(transaction) == null) return false;
                            if (field.getType() == String.class && ((String) field.get(transaction)).isBlank())
                                return false;
                        }
                        return true;
                    } catch (Exception e) {
                        System.out.println("removing transaction due to invalid data");
                        return false;
                    }
                })
                .distinct()
                .forEach(System.out::println);
    }

    /**
     * This class is designed to read in the CSV data file which will be processed by the
     * functional features Java has to offer - Stream API
     */
    static final class CsvReader {
        public List<Map<String, String>> read(String filePath) {
            List<Map<String, String>> data;

            try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
                List<String> headers = new ArrayList<>();

                data = lines
                        .peek(line -> {
                            if (headers.isEmpty())
                                headers.addAll(List.of(line.split(",")));
                        })
                        .skip(1)
                        .map(line -> {
                            String[] values = line.split(",");
                            Map<String, String> map = new HashMap<>();

                            for (int i = 0; i < headers.size(); i++) {
                                map.put(headers.get(i), values[i]);
                            }

                            return map;
                        })
                        .toList();
            } catch (IOException e) {
                System.err.println("Error reading file " + filePath);
                throw new RuntimeException(e);
            }

            return data;
        }
    }

    /**
     * This class is an object representation of the data being imported by the CSV Reader.
     * By converting the raw data into a Java object, it will allow me to perform more indepth data
     * processing than if handled in a raw format - for example, checking individual fields for valid data,
     * ensuring that the data object itself isn't empty or null etc...
     */
    static final class Transaction {
        private Date date;
        private String mode;
        private String category;
        private String subCategory;
        private String note;
        private Double amount;
        private String incomeOrExpense;
        private String currency;

        private static final Map<String, String> headerMapper = Map.of(
                "Date", "date",
                "Mode", "mode",
                "Category", "category",
                "Subcategory", "subCategory",
                "Note", "note",
                "Amount", "amount",
                "Income/Expense", "incomeOrExpense",
                "Currency", "currency"
        );

        public static Transaction fromMap(Map<String, String> map) {
            Transaction transaction = new Transaction();
            map.forEach((key, value) -> {
                try {
                    Field field = transaction.getClass().getDeclaredField(headerMapper.get(key));
                    field.setAccessible(true);
                    field.set(transaction, convertValue(field, value));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });

            return transaction;
        }

        private static Object convertValue(Field field, String value) {
            Class<?> fieldType = field.getType();

            try {
                if (fieldType == String.class) return value;
                if (fieldType == Double.class) return Double.parseDouble(value);
                if (fieldType == Date.class) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    try {
                        return sdf.parse(value);
                    } catch (ParseException e) {
                        return null;
                    }
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }

        public Date getDate() {
            return date;
        }

        public String getMode() {
            return mode;
        }

        public String getCategory() {
            return category;
        }

        public String getSubCategory() {
            return subCategory;
        }

        public String getNote() {
            return note;
        }

        public Double getAmount() {
            return amount;
        }

        public String getIncomeOrExpense() {
            return incomeOrExpense;
        }

        public String getCurrency() {
            return currency;
        }

        @Override
        public String toString() {
            return "Transaction{" +
                    "date=" + date +
                    ", mode='" + mode + '\'' +
                    ", catagory='" + category + '\'' +
                    ", subCatagory='" + subCategory + '\'' +
                    ", note='" + note + '\'' +
                    ", amount=" + amount +
                    ", incomeOrExpense=" + incomeOrExpense +
                    ", currency='" + currency + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Transaction that)) return false;
            return Objects.equals(date, that.date)
                    && Objects.equals(mode, that.mode)
                    && Objects.equals(category, that.category)
                    && Objects.equals(subCategory, that.subCategory)
                    && Objects.equals(note, that.note)
                    && Objects.equals(amount, that.amount)
                    && Objects.equals(incomeOrExpense, that.incomeOrExpense)
                    && Objects.equals(currency, that.currency);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, mode, category, subCategory, note, amount, incomeOrExpense, currency);
        }
    }
}