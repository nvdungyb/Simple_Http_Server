# mid-project-801277361

Đây là dự án giữa kỳ, chủ đề: HTTP server.
<br>

**Thành viên:**

- Nguyễn Văn Dũng - B21DCCN277
- Nguyễn Đức Hiếu - B21DCCN361
- Hoàng Gia Vương - B21DCCN801

## Mô tả

- Server nhận request từ client, xử lý và response.

### Dự án này bao gồm các phần sau:

- Server hỗ trợ các giao thức: HTTP/1.1 và HTTP/2
- Công nghệ: Java

## Tiến độ

### Tuần 1

- **Mục tiêu:** Server hỗ trợ giao thức HTTP/1.1 với GET và POST
- **Công việc đã làm:**
  <ul>
      <li>Xử lý đa luồng</li>
      <li>Đọc file cấu hình Json</li>
      <li>Phân tích Http request và gửi Http response</li>
      <li>Cho phép Upload tài nguyên lên server</li>
  </ul>

## Cài đặt

Để cài đặt dự án, làm theo các bước sau:

1. Download git: https://git-scm.com/download/win
2. Clone project về máy sử dụng git clone:
   Tạo 1 folder chứa file project sau đó bật git bash:
   Chuột phải vào folder > Show more option > Git Bash here

```sh
git clone https://github.com/jnp2018/801277361.git
cd mid-project-801277361

• Dùng git status để kiểm tra từng bước.
1. Trước khi bắt đầu làm task, checkout trở về nhánh main để pull. Chạy lệnh:
    o git checkout main
    o git pull origin main
2. Tạo nhánh mới để bắt đầu code:
    o git checkout -b <tên nhánh mới>
    o tạo xong thì bắt đầu code
3. Sau khi code xong:
    o git add .
    o git commit -m “tin nhắn”
4. Đẩy code mình vừa làm lên github và tạo pull request:
    o Git push origin < tên nhánh mình vừa làm >
    o Vào lại link project trên github và sẽ thấy “Compare & pull request”
    o Bấm vào để kiểm tra code mình sửa và bấm Create.



