package com.multiplechoice.Multiple.controller;

import com.multiplechoice.Multiple.Dtp.UserAcc;
import com.multiplechoice.Multiple.Dtp.UserVerify;
import com.multiplechoice.Multiple.model.Question;
import com.multiplechoice.Multiple.model.User;
import com.multiplechoice.Multiple.model.UserQuiz;
//import com.multiplechoice.Multiple.model.User;
import com.multiplechoice.Multiple.model.VerifyUser;
import com.multiplechoice.Multiple.service.QuestionService;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.*;

@RestController
@RequestMapping("/Questions")
@CrossOrigin
public class QuestionController {

    @Autowired
    private QuestionService questionService;
    @GetMapping("/")
    public String UserIndex()
    
    {
    	return "UserIndex";
    }
    
    @GetMapping("/Admin")
    public String Home()
    {
    	return "Admin";
    }
    
   // @GetMapping("/Question")
    @PostMapping("/create")
    public ResponseEntity<String> createQuestion(@RequestBody Question question) {
    	List<Question>check=questionService.getAllQuestions();
    	for(Question x:check)
    	{
    		if(x.getQuestiontext().equals(question.getQuestiontext()))
    			return ResponseEntity.status(HttpStatus.CONFLICT).body("Already Exist Question");
    	}
    	if(question.getQuestiontext()!=null && question.getOptionA()!=null && question.getOptionB()!=null && question.getOptionC()!=null && question.getOptionD()!=null && question.getCorrectAnswer()!=null && question.getType()!=null )
    	{
    		questionService.saveQuestion(question);
    		return ResponseEntity.ok("Done");
    	}
    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid");

    }
    
    @GetMapping("/create")
    public String showCreateQuestionForm(Model model) {
        model.addAttribute("question", new Question()); 
        return "createQuestion";
    }
    @GetMapping("/AllQuestions")
    public List<Question> getAllQuestions() {
        List<Question>s= questionService.getAllQuestions();
        return s;
        
    }

    @GetMapping("/UpdateQuestion/{id}")
    public String Updatequestion(@PathVariable Long id,Model model)
    {
    	Question question=questionService.getQuestionById(id);
    	 model.addAttribute("question", question);
    	 return "UpdateQuestion";
    	
    }
    @PostMapping("/UpdateQuestion")
    public String updateQuestion(@ModelAttribute Question question) {
        Question existingQuestion = questionService.getQuestionById(question.getId());
        existingQuestion.setQuestiontext(question.getQuestiontext());
        existingQuestion.setOptionA(question.getOptionA());
        existingQuestion.setOptionB(question.getOptionB());
        existingQuestion.setOptionC(question.getOptionC());
        existingQuestion.setOptionD(question.getOptionD());
        existingQuestion.setCorrectAnswer(question.getCorrectAnswer());
        existingQuestion.setType(question.getType());

        questionService.saveQuestion(existingQuestion);
        return "redirect:/Questions/AllQuestions";
    }
   // @GetMapping("/delete/{id}")
   // public String deleteQuestion(@PathVariable Long id) {
   //     questionService.deleteQuestionById(id);
    //    return "redirect:/Questions/AllQuestions";
    //}
    
    /*  @PostMapping("/User/SignUp")
    public String signup(@ModelAttribute User user)
    {
    	System.out.println(user.getEmail()); // Just to confirm data received
    	List<User>users=questionService.getAllUsers();
    	int flag=0;
    	for(User u:users)
    	{
    		if(u.getEmail().equals(user.getEmail()))
    			return "Error";
    	}
    	questionService.saveUser(user);
        return "redirect:/Questions/User/SignIn" ; // R
    	
    }
    
    
    @GetMapping("/User/SignUp")
    public String start(Model model)
    {
    	User user=new User();
    	model.addAttribute("user",user);
    	return "Signup";
    }*/
    @GetMapping("/Error")
    public String Error()
    {
    	return "Error";
    }
    
    @GetMapping("/User/SignIn")
    public String Signin(Model model)
    
    {
    	UserAcc userAcc=new UserAcc();
    	
    	model.addAttribute("userAcc",userAcc);
    	return "Signin";
    }
   /* @PostMapping("/User/SignIn")
    public String SignIn(@ModelAttribute UserAcc userAcc,HttpSession session)
    {
    	User u=questionService.getUserByEmail(userAcc.getEmail());
    	if(u.getEmail().equals("Invalid"))
    	{
    		return "redirect:/Questions/UserIndex";
    	}
    	else
    	{
    		if(u.getPassWord().equals(userAcc.getPassword()))
    		{
    			session.setAttribute("validUser", u);
    		}
    		else
    			return "redirect:/Questions/UserIndex";
    	}
    	
    	return "redirect:/Questions/User" ;
    }*/
    @PostMapping("User/SignUp")
    public ResponseEntity<String> SignUp(@RequestBody User u)
    {
    	boolean exist = questionService.findbyEmail(u.getEmail());
    			if(!exist) {
    	        questionService.saveUser(u);
    	        return ResponseEntity.ok("SignUp Done");
    			}
    			else
    			{
    				return ResponseEntity.status(HttpStatus.CONFLICT).body("User Already Exist");
    			}
    }
    @PostMapping("User/LogIn")
    public ResponseEntity<String> login(@RequestBody UserAcc u) {
        User user = questionService.getUserByEmail(u.getEmail());

        if (user.getEmail().equals("Invalid")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }


        if (u.getPassword().equals(user.getPassWord())) {
            VerifyUser v=questionService.FindVerifyUser(user.getId());
            if(v!=null && u.getRoll().equals("admin") && v.getAccept().equals("true"))
            return ResponseEntity.ok(""+user.getId());
            else
            {
                if(v==null && u.getRoll().equals("user"))
                    return ResponseEntity.ok(""+user.getId());
                else
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("need Permision As admin");

            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

   
    @GetMapping("/User")
    public String User(HttpSession session,Model model)
    {
    	User u=(User) session.getAttribute("validUser");
    	model.addAttribute("user", u);
    	return "UserPage";
    }
    @GetMapping("/User/userquiz/{type}")
    public List<Question> userquiztype(@PathVariable String type)
    
    {
    	List<Question>questions=questionService.getAllQuestions();
    	List<Question>Tquestion=new ArrayList<>();
    	for(Question q:questions)
    	{
    		if(q.getType().equals(type))
    			Tquestion.add(q);
    	}
    	Collections.shuffle(Tquestion);
    	
    	List<Question> ran20;
    	if(Tquestion.size()>19)
    	ran20=Tquestion.subList(0, 20);
    	else
    		ran20=Tquestion;
    	return ran20;
    }
    
    @GetMapping("/User/userquiz")
    public List<Question> userquiz()
    
    {
    	List<Question>questions=questionService.getAllQuestions();
    	Collections.shuffle(questions);
    	
    	List<Question> ran20;
    	if(questions.size()>19)
    	ran20=questions.subList(0, 20);
    	else
    		ran20=questions;
    	return ran20;
    }

    @GetMapping("/viewQuiz/{id}")
    public List<UserQuiz>display(@PathVariable Long id)
    {
    	User user=questionService.getUserById(id);
    	List<UserQuiz>s =new ArrayList<> ();
    	s=(List<UserQuiz>) questionService.viewQuiz();
    	List<UserQuiz>r=new ArrayList<>();
    	for(UserQuiz u:s)
    	{
    		if(user.getId().equals(u.UserId()))
    		{
    		  r.add(u);
    		}
    	}
    	return r;
    	
    	
    }
    @GetMapping("/email/{eid}")
    ResponseEntity<String> Email(@PathVariable("eid") Long eid)
    {
        User u=questionService.getUserById(eid);
        return ResponseEntity.ok(u.getEmail());
    }
    @GetMapping("/Tech")
        List<UserVerify>findusers()
        {
             List<User>u=questionService.findByRollAdmin("admin");
             List<UserVerify>uvf=new ArrayList<>();

             for(User us:u)
             {
                 VerifyUser v=new VerifyUser();
                 UserVerify uv=new UserVerify();
                 v=questionService.FindVerifyUser(us.getId());
                 uv.setId(us.getId());
                 uv.setEmail(us.getEmail());
                 uv.setRoll(us.getRoll());
                 if(v!=null)
                 {
                     uv.setAccept(v.getAccept());
                 }
                 else
                     uv.setAccept("false");

                 uvf.add(uv);


             }
             return uvf;
        }
    @PutMapping("/User/Accept/{id}")
    ResponseEntity<String>Accept(@PathVariable("id") Long id)
    {
        VerifyUser v=questionService.FindVerifyUser(id);
        User user=questionService.getUserById(id);
        if(v!=null) {
            if (v.getAccept().equals("false"))
                v.setAccept("true");
            else
                v.setAccept("false");
            questionService.saveVerifyUser(v);
           return ResponseEntity.ok("Updated");
        }
        else
        {
            VerifyUser vcs=new VerifyUser();
            vcs.setAccept("true");
            vcs.setUser(user);
            questionService.saveVerifyUser(vcs);
            return ResponseEntity.ok("Updated");
        }

    }
    @PostMapping("/User/SubmitQuiz")
    public void SubmitQuiz(HttpSession session,@RequestBody List<UserQuiz>userQuizList ) {
        if (userQuizList != null && !userQuizList.isEmpty()) {
           questionService.saveUserQuiz(userQuizList);
            
        } else {
            System.out.println("List is Empty");
        }
          // or any other view name you want to show
    }
    @PostMapping("/Result/{id}/{type}")
    public ResponseEntity<String> result(@PathVariable String type,@PathVariable Long id,@RequestBody List<UserQuiz> u)
    {
    	//List<Question>q=new ArrayList<>();
    	
    	double c=0;
        for (UserQuiz i : u) {
            Optional<Question> optionalQuestion = questionService.findById2(i.getQuestion().getId());
            if (optionalQuestion.isPresent()) {
                Question q = optionalQuestion.get();


                if (i.getMarked().equals(q.getCorrectAnswer())) {
                    c++;
                }
            }
        }


        if (!u.isEmpty()) {
            c = (c / u.size()) * 100;
        }
        return ResponseEntity.ok(Double.toString(c));


    }
    @GetMapping("/subjectresults/{id}/{type}")
    ResponseEntity<String> resultDeclare(@PathVariable("id") Long id,@PathVariable("type") String type)
    {
        List<UserQuiz>u=questionService.getUserQuestionType(type, id);
        double c=0;
        for (UserQuiz i : u) {
            Optional<Question> optionalQuestion = questionService.findById2(i.getQuestion().getId());
            if (optionalQuestion.isPresent()) {
                Question q = optionalQuestion.get();


                if (i.getMarked().equals(q.getCorrectAnswer())) {
                    c++;
                }
            }
        }


        if (!u.isEmpty()) {
            c = (c / u.size()) * 100;
        }
        return ResponseEntity.ok(Double.toString(c));
    }

}

   