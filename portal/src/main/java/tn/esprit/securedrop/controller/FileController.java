package tn.esprit.securedrop.controller;

import tn.esprit.securedrop.model.FileMetadata;
import tn.esprit.securedrop.service.FileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/files")
public class FileController {

    private final FileService fileService;

    public FileController(
            FileService fileService) {

        this.fileService = fileService;
    }

    @GetMapping
    public String listFiles(Model model) {

        model.addAttribute(
                "files",
                fileService.findAll()
        );

        return "files";
    }

    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

        try {

            FileMetadata metadata =
                    fileService.upload(file);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "File uploaded successfully and queued for scanning."
            );

            return "redirect:/files/"
                    + metadata.getId();

        } catch (Exception exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/files/upload";
        }
    }

    @GetMapping("/{id}")
    public String fileDetails(
            @PathVariable String id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            model.addAttribute(
                    "file",
                    fileService.findById(id)
            );

            return "file-details";

        } catch (Exception exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "redirect:/files";
        }
    }
}