package inhatc.cse.spring.controller;

import inhatc.cse.spring.dto.BookDto;
import inhatc.cse.spring.dto.BookResponseDto;
import inhatc.cse.spring.dto.MemberDto;
import inhatc.cse.spring.service.BookService;
import inhatc.cse.spring.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class MemberApiController {

    private final MemberService memberService;
    private final BookService bookService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberDto memberDto) {
        System.out.println("=======================>" + memberDto);
        boolean result = memberService.login(memberDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<?> memberInsert(@RequestBody MemberDto memberDto) {
        System.out.println("============" + memberDto);
        int result = memberService.save(memberDto);

        return new ResponseEntity<>(memberDto, HttpStatus.OK);
    }

    @GetMapping("memberlist")
    public ResponseEntity<?> memberList() {
        List<MemberDto> memberList = memberService.findAll();
        return new ResponseEntity<>(memberList, HttpStatus.OK);
    }

    @GetMapping("memberDetail/{id}")
    public ResponseEntity<?> memberIdList(@PathVariable int id) {
        MemberDto memberDto = memberService.findId(id);
        return new ResponseEntity<>(memberDto, HttpStatus.OK);
    }

    @PostMapping("/bookSave")
    public ResponseEntity<?> saveBook(
            @RequestPart("title") String title,
            @RequestPart("author") String author,
            @RequestPart("publisher") String publisher,
            @RequestPart("price") String price,
            @RequestPart("created_at") String created_at,
            @RequestPart("thumbnailPhoto") MultipartFile thumbnailPhoto) {

        // 한글 깨짐 방지
        title = new String(title.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        author = new String(author.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        publisher = new String(publisher.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        price = new String(price.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        created_at = new String(created_at.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        // log불가, 프린트로 확인 - 데이터 제대로 넘어오는지, 인코딩 확인
        System.out.println("=============컨트롤러 진입=============");
        System.out.println("title: " + title);
        System.out.println("author: " + author);
        System.out.println("publisher: " + publisher);
        System.out.println("price: " + price);
        System.out.println("created_at: " + created_at);
        System.out.println("thumbnailPhoto: " + thumbnailPhoto.getOriginalFilename());

        String file = saveFile(thumbnailPhoto);
        BookDto bookDto = BookDto.builder()
                .title(title)
                .author(author)
                .publisher(publisher)
                .price(price)
                .created_at(created_at)
                .thumbnailPhoto(file)
                .build();

        System.out.println("========================" + bookDto.toString());

        int result = bookService.save(bookDto);

        if (result > 0) {
            return new ResponseEntity<>(bookDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("책 등록 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String saveFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String filePath = "/Users/suhyeon/Documents/GitHub/SpringProject_2/src/main/java/inhatc/cse/spring/repository/images/" + fileName;
        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    @GetMapping("/bookList")
    public ResponseEntity<?> bookList() {
        List<BookResponseDto> bookList = bookService.findAll();
        System.out.println("bookList: " + bookList);
        return new ResponseEntity<>(bookList, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/image")
    public ResponseEntity<?> returnImage(@RequestParam("imageName") String imageName) {
        String path = "/Users/suhyeon/Documents/GitHub/SpringProject_2/src/main/java/inhatc/cse/spring/repository/images/"; //이미지가 저장된 위치

        Resource resource = new FileSystemResource(path + imageName);
        if (!resource.exists()) {
            return new ResponseEntity<>("이미지가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        HttpHeaders headers = new HttpHeaders();
        Path filePath = null;

        try {
            filePath = Paths.get(path + imageName);
            headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @GetMapping("/bookDetail/{id}")
    public ResponseEntity<?> bookDetail(@PathVariable int id) {
        System.out.println("상품 상세 조회: " + id + "번" + bookService.findById(id));
        BookResponseDto bookResponseDto = bookService.findById(id);
        return new ResponseEntity<>(bookResponseDto, HttpStatus.OK);
    }
}
