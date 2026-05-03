# Objectives
By doing this assignment, you will:
* Learn about opportunities for computing students and possible paths for improvement.
* Learn about SW design. Move from SRS (what) to design (how) to implementation (real SW)
* Learn how to develop architecture, class, state and sequence diagrams to describe a system.
* Learn how to enhance designs / code with SOLID principles and design patterns.
* Learn how to transform design models to code.
* Learn how to document code and to use GitHub as a repository for the project.

---

## Setup
* This assignment will be solved in groups of 4 students from the same lab. They do not have to be the same group who worked on Homework 1. تخلص من الأندال و ابحث عن الرجال
* Each team will design and implement part of a system for "Budget Management".
* Team will select one of the two SRS provided for "Budget Management".
* **TEAMS WILL WORK ON BUDGET APP NOT ON THEIR PROJECT FROM A1.**
* This is a big assignment that will require each team to work together closely and independently and will need self-learning and self-discipline. هذه المسألة هى مسك الختام للمقرر وتتطلب قدرا كبيرا من التعلم الذاتى والبحث وتنظيم الوقت والتعاون الجيد مع أعضاء الفريق.
* The group will submit together one combined solution.
* The entire group is responsible of helping any weak member to be able to do his/her task by his/herself, by providing the necessary support, knowledge, hands-on demos, etc.
* Only submit original work. Any copied (or AI-generated) work will be severely penalized. مسؤولية الفريق تضامنية عن عمله و أى غش من أى فرد سيكون مسؤولية الجميع و يخصم منهم مثل الدرجة
* Please read the marking criterion very carefully to understand how you will be marked.

---

## Task1: Opportunities for Students (1)
Research opportunities for students and make a post about them in classroom & write a report (add screen shot of the post). Each member will research 1 opportunity in each category below.
* Research 4 government / other training initiatives like ITI, NTI and others.
* Research 4 different jobs for software engineers. Explain each one & how can u do it freelancing
* Research and post 4 different internship opportunities that are still open to apply to. Put links.

---

## Task2: System Design (6)
Choose & read carefully 1 of the attached SRSs for Budgeting SW. If anything is unclear, ask the authors of SRS by email. They are the client. In real life u work with SRS done by someone else. SRS is not perfect but is a very good description of how the SW should behave & look like & the user tasks performed through the app. (We got 100 excellent SRSs. I had to select only 2 💚).
Read carefully the Software Design Specifications template given with this assignment. Read the instructions in red very carefully. Again, read red instructions to know how to fill the form.
Write the SDS document of your project, including all the required details. Use the template attached with this homework. DO NOT MAKE YOUR OWN.

### Task 2.1: System Architecture
* Identify all the main components or subsystems of the system and describe their role.
* Decide the suitable architectural design for your application.
* Draw an architectural diagram showing the different parts of the system using a suitable notation like C4 (Visualising software architecture with the C4 model - Simon Brown, Agile on the Beach 2019) or arrow and box. Top 5 Most Used Architecture Patterns.

### Task 2.2: Class Diagram
* Identify all the important classes in the system in each component.
* Divide these classes into packages or subsystems, each one includes the related classes.
* Decide the responsibility of each class and what role it plays.
* Design the attributes and operations of each class. For each attribute decide the type and visibility. For operations, decide the name, parameters and return type.
* Design the relations between classes (inheritance, association, aggregation, composition). For each relation, decide the direction and multiplicity and give it a label.
* Read again, the Software Design Specifications template given with this assignment.
* Finally, draw a complete class diagram for the system, divided into packages or systems. Use a tool for drawing like ArgoUML or https://www.plantuml.com/.

### Task 2.3: Sequence Diagrams
Sequence diagrams help developers understand how a system works and how a use case is implemented using the classes and methods in the class diagram.
* Select first seven user stories from the sample SRS chosen. (or the most important seven)
* Implement a detailed sequence diagram for each one of them.
* Read again, the Software Design Specifications template given with this assignment.

### Task 2.4: State Diagrams
State diagrams help developers understand the different possible states for an important object and the events that cause it to move it from a state to another.
* For the one important object, draw a state diagram to show the developer the different states it can be in.

### Task 2.5: SOLID and Design Patterns
* Demonstrate how your design applies 3 of SOLID principles and uses 3 Design Patterns.

---

## Task3: Implementation (6)
The objective of this task is to learn the process of transforming high-level design models (Structure and Interaction) into working, high-quality software.
1.  **Implementation Scope & Technology:**
    *   Teams are free to choose their technology stack (Desktop, Mobile, or Web) as long as it supports OOP. Implement the functions associated with the first 7 user stories in the SRS. Your code must work correctly , use OOP, and persist data using a Database or files. System should load data stored last time when it starts. Technology Options:
        *   **Desktop:** Java (Swing/JavaFX), C# (.NET/WPF).
        *   **Mobile:** Flutter (Dart), Android (Java/Kotlin), Swift.
        *   **Web:** Python (Django), TypeScript (NestJS/Node.js).
2.  **Sequence Diagram Mapping:**
    *   You must implement the logic described in the Sequence Diagrams. Every method call or message exchange shown in your diagrams must be reflected in your source code. The Class-Sequence Usage Table in your SDS must act as a map for the TA to find these interactions in your code.
3.  **GitHub & Version Control:**
    *   Create a private repository on GitHub for your project. Use this repo for development by doing multiple, meaningful commits from every team member. Create a PDF file containing snapshots of the GitHub Repo (contribution graphs and commit history) for every team member. This proves everyone participated in the coding process.

---

## Task 4: Documentation, Clean Code and Presentation (1)
* Document all classes and functions using a tool relevant to your language (e.g., JavaDoc for Java, Doxygen for C#, TypeDoc for TypeScript, DartDoc for Flutter, etc.).
* Generate these documentation pages and include the output folder in your submission.
* Review code quality and readability & apply Java Coding Style manual attached with A2. Or follow the official Coding Style Manual for your chosen language (e.g., PEP 8 for Python).
* Develop a 10 minutes presentation (10 - 15 slides) to present your work to the TA.
* Include a pdf or ppt of the slides in your submission.

---

## Task 5: More Implementation (…)
* Implement more functionality and negotiate with TA a bonus for the extra parts u did.

---

## Deliver and Assessment
1. Upload the SDS draft version by 22 April 2026 as a pdf file. Draft version should include the architecture diagram, the class diagram and class responsibility table and sequence diagrams with a cover page with team names, IDs, emails, course name and number and document topic and title. File name must be `CS251-2026-SectionNumber-TAName-IDs-DraftSDS.pdf` for example `CS251-2026-S1-AhmedSamir-20240001-20240002-20240003-DraftSDS.pdf`
2. On 2 May 2025 load the final submission including SDS final version `CS251-2026-SectionNumber-TAName-IDs-SDS.pdf` + code and documentation + presentation. You will upload 1 zip file with:
    * A report of the opportunities u found.
    * A pdf file containing your design (SDS) document.
    * A directory with your implementation and documentation. Add a `Readme.txt` file explaining the files included and the tools used to develop the program. (no exe or jar)
    * The Generated Documentation Folder (HTML/PDF output from your doc tool).
    * Pdf or ppt of your presentation + screenshots of GitHub
    * All pdf files need a cover page with team details (names, emails, phone, title, etc.)

---

## Marking criteria

### Opportunities for Students (1)
* **1 mark:** For excellent report/post of (1) initiatives, (2) careers, (3) internships

### Software Design Specifications (6)
* **1 mark:** For excellent well-designed and explained architecture diagram.
* **1.5 marks:** For an excellent class diagram and class description that correctly captures most of the classes, their details and their relations
* **1.5 marks:** For excellent 7 sequence diagrams and class-sequence table.
* **0.5 mark:** For an excellent state diagram
* **1.5 mark:** For an excellent use of SOLID and DPs and explaining that
* **-6.0:** For copied or non-original report or giving yours to another group.
* **-1.0:** Disorganized - badly written reports and no cover page.

### Implementation (6)
* **3.5 marks:** For excellent working code that does the required functions.
* **2 mark:** For proper mapping and traceability between model and code
* **0.5 mark:** For use of GitHub and frequent commits on it.
* **-6:** For copied or non-original code or giving yours to another group.

### Documentation, Clean Code and Presentation Slides (1)
* **1 mark:** JavaDoc pages created for all classes and methods with useful info. Java Coding Style is accurately followed and code is readable. Power point / pdf presentation of your work

---

## Policy Regarding Plagiarism:
1. تشجع الكلية على مناقشة الأفكار و تبادل المعلومات و مناقشات الطلاب حيث يعتبر هذا جوهريا لعملية تعليمية سليمة
2. ساعد زملاءك على قدر ما تستطيع و حل لهم مشاكلهم فى الكود و لكن تبادل الحلول غير مقبول و يعتبر غشا.
3. أى حل يتشابه مع أى حل آخر بدرجة تقطع بأنهما منقولان من نفس المصدر سيعتبر أن صاحبيهما قد قاما بالغش.
4. استعمال أدوات الذكاء الاصطناعى لحل هذه المسألة يعد غشا (تدرب على هذه الأدوات لمستقبلك لكن ليس فى المسألة)
5. إذا لم تكن متأكدا أن فعلا ما يعد غشا فلتسأل المعيد أو أستاذ المادة.
6. فى حالة ثبوت الغش سيأخذ الطالب سالب درجة المسألة ، و فى حالة تكرار الغش سيرسب الطالب فى المقرر.
