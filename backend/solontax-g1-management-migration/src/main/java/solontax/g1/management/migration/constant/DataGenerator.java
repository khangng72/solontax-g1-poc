package solontax.g1.management.migration.constant;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class DataGenerator {
    private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    private static final List<String> sampleFirstNameList = List.of(
            "An", "Anh", "Bảo", "Bình", "Châu", "Chi", "Cường", "Dũng", "Duy", "Giang",
            "Hà", "Hải", "Hân", "Hiếu", "Hiền", "Hoa", "Hoài", "Hoàng", "Hồng", "Hương",
            "Huy", "Hùng", "Hạnh", "Khánh", "Khoa", "Kiên", "Lan", "Linh", "Loan", "Long",
            "Mai", "Minh", "My", "Nam", "Nga", "Ngân", "Ngọc", "Nghĩa", "Nhi", "Nhung",
            "Oanh", "Phát", "Phong", "Phúc", "Phương", "Quang", "Quốc", "Quỳnh", "Sơn", "Tài",
            "Tâm", "Tân", "Tấn", "Thảo", "Thành", "Thắng", "Thịnh", "Thiện", "Thu",
            "Thư", "Thủy", "Thúy", "Tiên", "Toàn", "Trang", "Trinh", "Trọng", "Trúc", "Tuấn",
            "Tú", "Tùng", "Uyên", "Vân", "Vinh", "Việt", "Yến", "Nhật", "Đạt", "Đức",
            "Ánh", "Băng", "Bích", "Cẩm", "Công", "Diễm", "Diệp", "Đông", "Hạo", "Khôi",
            "Lam", "Lộc", "Ly", "Minh", "Nguyên", "Nhật", "Phượng", "Quân", "Thanh", "Tường"
    );

    private static final List<String> sampleLastNameList = List.of(
            "Nguyễn", "Trần", "Lê", "Phạm", "Huỳnh", "Hoàng", "Phan", "Vũ", "Võ", "Đặng",
            "Bùi", "Đỗ", "Hồ", "Ngô", "Dương", "Lý", "Mai", "Trương", "Đinh", "Đoàn",
            "Lâm", "Đào", "Tạ", "Văn", "Lưu", "Châu", "Phùng", "Tô", "Cao", "Đậu",
            "Tăng", "La", "Đàm", "Lại", "Đinh", "Triệu", "Hà", "Thái", "Lương", "Chu",
            "Thạch", "Hứa", "Khuất", "Mạc", "Bạch", "Ngụy", "Âu", "Từ", "Đoàn", "Dư",
            "Tống", "Vương", "Giang", "Quách", "Trịnh", "Phước", "Lộc", "Huệ", "Thiệu", "Uyên",
            "Đinh", "Phú", "Đông", "Đài", "Bành", "Liễu", "Trần", "Ngân", "Khưu", "Nguyễn Hữu",
            "Nguyễn Văn", "Nguyễn Thị", "Lê Văn", "Lê Thị", "Trần Văn", "Trần Thị", "Phạm Văn", "Phạm Thị", "Hoàng Văn", "Hoàng Thị",
            "Bùi Văn", "Bùi Thị", "Đỗ Văn", "Đỗ Thị", "Ngô Văn", "Ngô Thị", "Dương Văn", "Dương Thị", "Đặng Văn", "Đặng Thị",
            "Võ Văn", "Võ Thị", "Vũ Văn", "Vũ Thị", "Phan Văn", "Phan Thị", "Lý Văn", "Lý Thị", "Huỳnh Văn", "Huỳnh Thị"
    );

    public static String randomLastName() {
        return sampleLastNameList.get(ThreadLocalRandom.current().nextInt(sampleLastNameList.size()));
    }

    public static String randomFirstName() {
        return sampleFirstNameList.get(ThreadLocalRandom.current().nextInt(sampleFirstNameList.size()));
    }

    public static Long randomTaxNumber() {
        return counter.incrementAndGet();
    }

    public static LocalDate randomDateOfBirth() {
        LocalDate start = LocalDate.of(1945, 1, 1);
        LocalDate end = LocalDate.of(2010, 12, 31);

        long startEpochDay = start.toEpochDay();
        long endEpochDay = end.toEpochDay();

        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);
        return LocalDate.ofEpochDay(randomDay);
    }

}
