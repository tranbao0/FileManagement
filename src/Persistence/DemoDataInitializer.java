package Persistence;

import Management.FileSystem;
import Model.File;

/**
 * Demo data initializer for showcase purposes.
 * Creates a variety of files demonstrating all system features including storage.
 *
 * To use: Call DemoDataInitializer.initialize(fileSystem) on a fresh FileSystem
 */
public class DemoDataInitializer {

    public static void initialize(FileSystem fs) {
        System.out.println("Initializing demo data...\n");

        // ============================================
        // CATEGORY 1: School/Study Files
        // ============================================

        fs.createFile("Math_Chapter5_Notes.txt");
        fs.writeFileContent("Math_Chapter5_Notes.txt",
                "CALCULUS CHAPTER 5: INTEGRATION\n" +
                        "================================\n\n" +
                        "Key Concepts:\n" +
                        "- Definite integrals represent area under curves\n" +
                        "- Fundamental Theorem of Calculus connects derivatives and integrals\n" +
                        "- Integration by parts: ∫u dv = uv - ∫v du\n\n" +
                        "Practice Problems:\n" +
                        "1. ∫(3x² + 2x) dx = x³ + x² + C\n" +
                        "2. ∫sin(x) dx = -cos(x) + C");
        fs.addTag("Math_Chapter5_Notes.txt", "notes");
        fs.addTag("Math_Chapter5_Notes.txt", "math");
        fs.addTag("Math_Chapter5_Notes.txt", "school");

        fs.createFile("Physics_Waves_Notes.txt");
        fs.writeFileContent("Physics_Waves_Notes.txt",
                "PHYSICS: WAVE MECHANICS\n" +
                        "=======================\n\n" +
                        "Wave Properties:\n" +
                        "- Wavelength (λ): distance between peaks\n" +
                        "- Frequency (f): waves per second (Hz)\n" +
                        "- Velocity: v = λf\n\n" +
                        "Types of Waves:\n" +
                        "1. Transverse - oscillation perpendicular to direction\n" +
                        "2. Longitudinal - oscillation parallel to direction\n\n" +
                        "Examples: Light (transverse), Sound (longitudinal)");
        fs.addTag("Physics_Waves_Notes.txt", "notes");
        fs.addTag("Physics_Waves_Notes.txt", "physics");
        fs.addTag("Physics_Waves_Notes.txt", "school");

        fs.createFile("Chemistry_Bonds_Notes.txt");
        fs.writeFileContent("Chemistry_Bonds_Notes.txt",
                "CHEMISTRY: CHEMICAL BONDING\n" +
                        "===========================\n\n" +
                        "Types of Bonds:\n\n" +
                        "1. IONIC BONDS\n" +
                        "   - Transfer of electrons\n" +
                        "   - Metal + Nonmetal\n" +
                        "   - Example: NaCl (table salt)\n\n" +
                        "2. COVALENT BONDS\n" +
                        "   - Sharing of electrons\n" +
                        "   - Nonmetal + Nonmetal\n" +
                        "   - Example: H₂O (water)\n\n" +
                        "3. METALLIC BONDS\n" +
                        "   - Sea of electrons\n" +
                        "   - Metal + Metal\n" +
                        "   - Example: Fe (iron)");
        fs.addTag("Chemistry_Bonds_Notes.txt", "notes");
        fs.addTag("Chemistry_Bonds_Notes.txt", "chemistry");
        fs.addTag("Chemistry_Bonds_Notes.txt", "school");

        // ============================================
        // CATEGORY 2: Project Files
        // ============================================

        fs.createFile("Project_Proposal.txt");
        fs.writeFileContent("Project_Proposal.txt",
                "SOFTWARE PROJECT PROPOSAL\n" +
                        "=========================\n\n" +
                        "Project Title: File Management System Simulator\n\n" +
                        "Objective:\n" +
                        "Create a simulated operating system file manager with features\n" +
                        "including block-based storage, version history, tag-based \n" +
                        "organization, and recycle bin functionality.\n\n" +
                        "Key Features:\n" +
                        "• Block-based virtual disk storage\n" +
                        "• Create, edit, delete files\n" +
                        "• Tag-based search and organization\n" +
                        "• Version history with restore capability\n" +
                        "• Recycle bin for safe deletion\n" +
                        "• Persistent storage\n\n" +
                        "Timeline: 4 weeks\n" +
                        "Status: COMPLETED");
        fs.addTag("Project_Proposal.txt", "project");
        fs.addTag("Project_Proposal.txt", "important");

        fs.createFile("Project_TODO.txt");
        fs.writeFileContent("Project_TODO.txt",
                "PROJECT TO-DO LIST\n" +
                        "==================\n\n" +
                        "[✓] Design system architecture\n" +
                        "[✓] Implement Storage class (block-based)\n" +
                        "[✓] Implement File and FileVersion models\n" +
                        "[✓] Implement Directory management\n" +
                        "[✓] Implement OpenFileTable\n" +
                        "[✓] Implement RecycleBin\n" +
                        "[✓] Implement FileSystem orchestrator\n" +
                        "[✓] Integrate Storage with FileSystem\n" +
                        "[✓] Add tag-based search\n" +
                        "[✓] Add version history\n" +
                        "[✓] Implement persistence layer\n" +
                        "[✓] Build GUI interface\n" +
                        "[✓] Add storage statistics panel\n" +
                        "[✓] Testing and bug fixes\n\n" +
                        "ALL TASKS COMPLETED!");
        fs.addTag("Project_TODO.txt", "project");
        fs.addTag("Project_TODO.txt", "todo");

        // ============================================
        // CATEGORY 3: Personal Files
        // ============================================

        fs.createFile("Shopping_List.txt");
        fs.writeFileContent("Shopping_List.txt",
                "GROCERY SHOPPING LIST\n" +
                        "=====================\n\n" +
                        "Produce:\n" +
                        "• Apples\n" +
                        "• Bananas\n" +
                        "• Spinach\n" +
                        "• Tomatoes\n\n" +
                        "Dairy:\n" +
                        "• Milk\n" +
                        "• Cheese\n" +
                        "• Yogurt\n\n" +
                        "Other:\n" +
                        "• Bread\n" +
                        "• Pasta\n" +
                        "• Olive oil");
        fs.addTag("Shopping_List.txt", "personal");
        fs.addTag("Shopping_List.txt", "todo");

        fs.createFile("Recipe_Pasta.txt");
        fs.writeFileContent("Recipe_Pasta.txt",
                "HOMEMADE PASTA RECIPE\n" +
                        "=====================\n\n" +
                        "Ingredients:\n" +
                        "- 2 cups all-purpose flour\n" +
                        "- 3 large eggs\n" +
                        "- 1 tbsp olive oil\n" +
                        "- 1/2 tsp salt\n\n" +
                        "Instructions:\n" +
                        "1. Make a well in the flour\n" +
                        "2. Add eggs, oil, and salt to the well\n" +
                        "3. Mix until dough forms\n" +
                        "4. Knead for 10 minutes\n" +
                        "5. Rest for 30 minutes\n" +
                        "6. Roll out and cut into desired shape\n" +
                        "7. Cook in boiling water for 2-3 minutes\n\n" +
                        "Serves: 4 people\n" +
                        "Prep time: 45 minutes");
        fs.addTag("Recipe_Pasta.txt", "personal");
        fs.addTag("Recipe_Pasta.txt", "recipe");

        // ============================================
        // CATEGORY 4: Work/Professional Files
        // ============================================

        fs.createFile("Meeting_Notes_Dec2024.txt");
        fs.writeFileContent("Meeting_Notes_Dec2024.txt",
                "TEAM MEETING NOTES - December 2024\n" +
                        "==================================\n\n" +
                        "Date: December 5, 2024\n" +
                        "Attendees: Team Alpha\n\n" +
                        "Agenda:\n" +
                        "1. Project status update\n" +
                        "2. Q1 planning\n" +
                        "3. Resource allocation\n\n" +
                        "Action Items:\n" +
                        "• John: Complete documentation by Dec 15\n" +
                        "• Sarah: Review budget proposal\n" +
                        "• Mike: Schedule client demo\n\n" +
                        "Next Meeting: December 12, 2024");
        fs.addTag("Meeting_Notes_Dec2024.txt", "work");
        fs.addTag("Meeting_Notes_Dec2024.txt", "notes");
        fs.addTag("Meeting_Notes_Dec2024.txt", "important");

        fs.createFile("Contact_List.txt");
        fs.writeFileContent("Contact_List.txt",
                "IMPORTANT CONTACTS\n" +
                        "==================\n\n" +
                        "Professor Smith (Math)\n" +
                        "  Email: smith@university.edu\n" +
                        "  Office: Room 205\n\n" +
                        "Dr. Johnson (Physics)\n" +
                        "  Email: johnson@university.edu\n" +
                        "  Office: Room 312\n\n" +
                        "IT Help Desk\n" +
                        "  Email: helpdesk@university.edu\n" +
                        "  Phone: 555-0123\n\n" +
                        "Library Services\n" +
                        "  Email: library@university.edu\n" +
                        "  Hours: 8am - 10pm");
        fs.addTag("Contact_List.txt", "work");
        fs.addTag("Contact_List.txt", "important");

        // ============================================
        // CATEGORY 5: Code/Technical Files
        // ============================================

        fs.createFile("Code_Snippets.txt");
        fs.writeFileContent("Code_Snippets.txt",
                "USEFUL CODE SNIPPETS\n" +
                        "====================\n\n" +
                        "// Java: Read file to string\n" +
                        "String content = Files.readString(Path.of(\"file.txt\"));\n\n" +
                        "// Java: Write string to file\n" +
                        "Files.writeString(Path.of(\"file.txt\"), content);\n\n" +
                        "// Java: Current timestamp\n" +
                        "LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);\n\n" +
                        "// Java: Simple HTTP request\n" +
                        "HttpClient client = HttpClient.newHttpClient();\n" +
                        "HttpRequest request = HttpRequest.newBuilder()\n" +
                        "    .uri(URI.create(\"https://api.example.com\"))\n" +
                        "    .build();");
        fs.addTag("Code_Snippets.txt", "code");
        fs.addTag("Code_Snippets.txt", "reference");

        fs.createFile("Git_Commands.txt");
        fs.writeFileContent("Git_Commands.txt",
                "GIT QUICK REFERENCE\n" +
                        "===================\n\n" +
                        "Basic Commands:\n" +
                        "  git init              - Initialize repository\n" +
                        "  git clone <url>       - Clone repository\n" +
                        "  git status            - Check status\n" +
                        "  git add .             - Stage all changes\n" +
                        "  git commit -m \"msg\"   - Commit changes\n" +
                        "  git push              - Push to remote\n" +
                        "  git pull              - Pull from remote\n\n" +
                        "Branching:\n" +
                        "  git branch            - List branches\n" +
                        "  git branch <name>     - Create branch\n" +
                        "  git checkout <name>   - Switch branch\n" +
                        "  git merge <name>      - Merge branch\n\n" +
                        "Useful:\n" +
                        "  git log --oneline     - Compact history\n" +
                        "  git diff              - Show changes\n" +
                        "  git stash             - Stash changes");
        fs.addTag("Git_Commands.txt", "code");
        fs.addTag("Git_Commands.txt", "reference");

        // ============================================
        // LARGE FILE FOR STORAGE DEMO (multiple blocks)
        // ============================================

        fs.createFile("Large_Document.txt");
        StringBuilder largeContent = new StringBuilder();
        largeContent.append("LARGE DOCUMENT FOR STORAGE DEMONSTRATION\n");
        largeContent.append("========================================\n\n");
        largeContent.append("This file is intentionally large to demonstrate\n");
        largeContent.append("how the storage system allocates multiple blocks.\n\n");

        // Add enough content to span multiple 512-byte blocks
        for (int i = 1; i <= 20; i++) {
            largeContent.append("--- Section ").append(i).append(" ---\n");
            largeContent.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n");
            largeContent.append("Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n");
            largeContent.append("Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.\n");
            largeContent.append("Duis aute irure dolor in reprehenderit in voluptate velit esse.\n\n");
        }
        largeContent.append("END OF LARGE DOCUMENT\n");

        fs.writeFileContent("Large_Document.txt", largeContent.toString());
        fs.addTag("Large_Document.txt", "demo");
        fs.addTag("Large_Document.txt", "storage");

        // ============================================
        // FILE WITH VERSION HISTORY
        // ============================================

        fs.createFile("Draft_Essay.txt");

        // Version 1
        fs.writeFileContent("Draft_Essay.txt",
                "ESSAY DRAFT - Version 1\n" +
                        "=======================\n\n" +
                        "Introduction:\n" +
                        "This is my first draft. Just getting ideas down.");
        simulateVersionSave(fs, "Draft_Essay.txt");

        // Version 2
        fs.writeFileContent("Draft_Essay.txt",
                "ESSAY DRAFT - Version 2\n" +
                        "=======================\n\n" +
                        "Introduction:\n" +
                        "Technology has transformed how we communicate.\n" +
                        "This essay explores the impact of social media.");
        simulateVersionSave(fs, "Draft_Essay.txt");

        // Version 3
        fs.writeFileContent("Draft_Essay.txt",
                "ESSAY DRAFT - Version 3\n" +
                        "=======================\n\n" +
                        "Introduction:\n" +
                        "Technology has fundamentally transformed human communication.\n" +
                        "This essay explores the profound impact of social media on\n" +
                        "modern society and interpersonal relationships.\n\n" +
                        "Body Paragraph 1:\n" +
                        "Social media platforms have connected billions of people...");
        simulateVersionSave(fs, "Draft_Essay.txt");

        // Version 4 (current)
        fs.writeFileContent("Draft_Essay.txt",
                "THE IMPACT OF SOCIAL MEDIA ON MODERN COMMUNICATION\n" +
                        "==================================================\n\n" +
                        "Introduction:\n" +
                        "Technology has fundamentally transformed human communication.\n" +
                        "This essay explores the profound impact of social media on\n" +
                        "modern society and interpersonal relationships.\n\n" +
                        "Body Paragraph 1: Connectivity\n" +
                        "Social media platforms have connected billions of people\n" +
                        "across geographical boundaries, enabling instant communication.\n\n" +
                        "Body Paragraph 2: Challenges\n" +
                        "However, this connectivity comes with challenges including\n" +
                        "privacy concerns and the spread of misinformation.\n\n" +
                        "Conclusion:\n" +
                        "While social media offers unprecedented connectivity,\n" +
                        "we must navigate its challenges thoughtfully.\n\n" +
                        "[FINAL DRAFT - Ready for submission]");
        simulateVersionSave(fs, "Draft_Essay.txt");

        fs.addTag("Draft_Essay.txt", "school");
        fs.addTag("Draft_Essay.txt", "essay");
        fs.addTag("Draft_Essay.txt", "important");

        // ============================================
        // FILES FOR RECYCLE BIN DEMO
        // ============================================

        fs.createFile("Old_Notes_ToDelete.txt");
        fs.writeFileContent("Old_Notes_ToDelete.txt",
                "These are old notes that are no longer needed.\n" +
                        "This file will be moved to the recycle bin for demo.\n" +
                        "Notice: Storage blocks remain allocated until permanent deletion!");
        fs.addTag("Old_Notes_ToDelete.txt", "old");
        fs.deleteFile("Old_Notes_ToDelete.txt");

        fs.createFile("Temp_File.txt");
        fs.writeFileContent("Temp_File.txt",
                "Temporary file - can be deleted\n" +
                        "This demonstrates the recycle bin restore feature.\n" +
                        "When restored, storage blocks are preserved.");
        fs.addTag("Temp_File.txt", "temp");
        fs.deleteFile("Temp_File.txt");

        fs.createFile("Backup_Data.txt");
        fs.writeFileContent("Backup_Data.txt",
                "Old backup data - no longer needed\n" +
                        "Demonstrates permanent delete from recycle bin.\n" +
                        "Permanent deletion will FREE the storage blocks!");
        fs.deleteFile("Backup_Data.txt");

        // ============================================
        // SUMMARY
        // ============================================

        System.out.println("Demo data initialized successfully!\n");
        System.out.println("=== Summary ===");
        System.out.println("Active files: " + fs.getFileCount());
        System.out.println("Deleted files: " + fs.getDeletedFileCount());
        System.out.println("\n=== Storage Statistics ===");
        System.out.println("Total blocks: " + fs.getStorageTotalBlocks());
        System.out.println("Used blocks: " + fs.getStorageUsedBlocks());
        System.out.println("Free blocks: " + fs.getStorageFreeBlocks());
        System.out.println("Usage: " + String.format("%.2f%%", fs.getStorageUsagePercentage()));
        System.out.println("\n=== Files by Category ===");
        System.out.println("• School/Notes: Math, Physics, Chemistry notes + Essay draft");
        System.out.println("• Project: Proposal, TODO list");
        System.out.println("• Personal: Shopping list, Recipe");
        System.out.println("• Work: Meeting notes, Contact list");
        System.out.println("• Code: Snippets, Git commands");
        System.out.println("• Storage Demo: Large_Document.txt (multi-block file)");
        System.out.println("\n=== Special Demo Files ===");
        System.out.println("• Draft_Essay.txt - Has 4 versions for version history demo");
        System.out.println("• Large_Document.txt - Spans multiple storage blocks");
        System.out.println("• 3 files in recycle bin for restore/delete demo");
    }

    private static void simulateVersionSave(FileSystem fs, String fileName) {
        int fd = fs.openFile(fileName, Model.OpenFileTableEntry.AccessMode.READ_WRITE);
        if (fd != -1) {
            fs.closeFile(fd);
        }
    }

    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        initialize(fs);

        boolean saved = FileSystemPersistence.save(fs, "demo_filesystem.dat");
        if (saved) {
            System.out.println("\nDemo data saved to 'demo_filesystem.dat'");
            System.out.println("Copy this file to 'filesystem.dat' to use it.");
        }
    }
}