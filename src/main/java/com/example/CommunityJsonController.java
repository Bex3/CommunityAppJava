package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bearden-tellez on 10/10/16.
 */

@RestController
public class CommunityJsonController {

    @Autowired
    MemberRepository members;

    @Autowired
    PostRepository posts;

    @Autowired
    EventRepository events;

    @Autowired
    MemberEventRepository memberevents;

    @Autowired
    OrganizationRepository organizations;

    @Autowired
    OrganizationMemberRepository organizationMembers;

    @Autowired
    InvitationRepository invitations;

    @RequestMapping(path = "/createDemoData.json", method = RequestMethod.POST)
    public void adminUser(HttpSession session) throws Exception {
        Member demoMember = new Member();

        demoMember.firstName = "Carlton";
        demoMember.lastName = "Banks";
        demoMember.email = "belair@gmail.com";
        demoMember.password = "mypassword";
        demoMember.streetAddress = "382 Penn Ave ";
        members.save(demoMember);

        Organization demoOrg = new Organization();
        demoOrg.name= "Debate Team";
        organizations.save(demoOrg);

        OrganizationMember newOrgMember = new OrganizationMember(demoOrg, demoMember);
        organizationMembers.save(newOrgMember);

        Post carltonPost = new Post();
        carltonPost.title = "How to debate";
        carltonPost.organization = demoOrg;
        carltonPost.date = "10/28/2016 ~ 17:00";
        carltonPost.body = "1. Research your subject 2. Create arguments for and against ...";
        carltonPost.author = demoMember;

        Event carltonEvent = new Event();
        carltonEvent.name = "Dance Lessons";
        carltonEvent.location = "The W";
        carltonEvent.organizer = demoMember;
        carltonEvent.date = "11/10/2016 ~ 13:00";
        carltonEvent.organization = demoOrg;
        carltonEvent.information = "Ever wondered how I dance as well as I do? Well come to the W and learn!";
        events.save(carltonEvent);

        Organization girlScoutOrg = new Organization();
        girlScoutOrg.name ="Girl Scout Troop 14565";
        organizations.save(girlScoutOrg);

        Organization puzzleOrg = new Organization();
        puzzleOrg.name = "Puzzle Group";
        organizations.save(puzzleOrg);

        Organization sportsOrg = new Organization();
        sportsOrg.name= "Basketball Team";
        organizations.save(sportsOrg);

        Organization jugOrg = new Organization();
        jugOrg.name = "Java Users Group Atlanta";
        organizations.save(jugOrg);

        Member wsMember = new Member();
        wsMember.firstName = "Will";
        wsMember.lastName = "Smith";
        wsMember.streetAddress = "382 Penn Ave";
        wsMember.email= "thefreshprince@gmail.com";
        wsMember.password= "basketball";
        members.save(wsMember);

        OrganizationMember wsToSports = new OrganizationMember(sportsOrg, wsMember);
        organizationMembers.save(wsToSports);

        Event bbGame = new Event();
        bbGame.name = "WildCats vs Panthers";
        bbGame.date = "11/2/2016 ~ 19:00";
        bbGame.location = "GSU";
        bbGame.organization = sportsOrg;
        bbGame.organizer = wsMember;
        events.save(bbGame);

        
    }


    @RequestMapping(path = "/login.json", method = RequestMethod.POST)
    public MemberResponseContainer login(HttpSession session, @RequestBody Member member) throws Exception {
        MemberResponseContainer myResponse = new MemberResponseContainer();
        Member newMember = members.findFirstByEmail(member.email);
        try {
            if (newMember == null) {
                myResponse.errorMessage = "User does not exist or was input incorrectly";
                System.out.println(" Username was null");
            } else if (!member.password.equals(newMember.getPassword())) {
                myResponse.errorMessage = "Incorrect password";
                System.out.println("Password attempt failed. Incorrect password");
            } else if (newMember != null && newMember.password.equals(newMember.getPassword())) {
                System.out.println(newMember.firstName + " " + newMember.lastName + " is logging in");
                if (newMember.photoURL == null) {
                    newMember.setPhotoURL("dummy photo URL");
                }
                session.setAttribute("member", newMember);
                myResponse.responseMember = newMember;
            }
        }catch (Exception ex) {
            myResponse.setErrorMessage("An exception occurred while logging in");
            ex.printStackTrace();
        }
        return myResponse;
    }

    @RequestMapping(path = "/register.json", method = RequestMethod.POST)
    public MemberResponseContainer newMemberInfo(HttpSession session, @RequestBody Member member) throws Exception {
        MemberResponseContainer myResponse = new MemberResponseContainer();
        Member newMember = members.findFirstByEmail(member.email);
        System.out.println(member.email + " is about to be created");
        try {
            if (newMember == null) {
                ArrayList<Invitation> listInvites = invitations.findByInvitedEmail(member.getEmail());
                int size = listInvites.size();
                if (size>=1) {
                    ArrayList<Invitation> allInvites = invitations.findByInvitedEmail(member.getEmail());
                    for (Invitation currentInvite : allInvites) {
                        Organization organization = currentInvite.getOrganization();
                        member = new Member(member.firstName, member.lastName, member.email, member.password, member.streetAddress, member.photoURL);
                        if (member.photoURL == null) {
                            member.setPhotoURL("dummy photo URL");
                        }
                        members.save(member);
                        OrganizationMember organizationMemberAssociation = new OrganizationMember(organization, member);
                        organizationMemberAssociation.setOrganization(organization);
                        organizationMembers.save(organizationMemberAssociation);
                        myResponse.responseMember = member;
                    }
                } else {
                    member = new Member(member.firstName, member.lastName, member.email, member.password, member.streetAddress, member.photoURL);
                    if (member.photoURL == null) {
                        member.setPhotoURL("dummy photo URL");
                    }
                    members.save(member);
                    myResponse.responseMember = member;
                    session.setAttribute("member", member);
                    //later they would create an org
                }
            } else {
                    myResponse.setErrorMessage("User already exists");
            }
        }catch (Exception ex) {
            myResponse.setErrorMessage("An exception occurred while registering");
            ex.printStackTrace();
        }
        return myResponse;
    }

    @RequestMapping(path = "/createPost.json", method = RequestMethod.POST)
    public PostContainer createPost(HttpSession session, @RequestBody Post post) {
//        Member member = (Member) session.getAttribute("member");
        Member author = (Member) session.getAttribute("member");  //changed member to author
        PostContainer postContainer = new PostContainer();
        post = new Post(post.date, post.title, post.body);
        try {
            if (post == null) {
                postContainer.setErrorMessage("Post was empty and therefore cannot be saved");

            } else {
                post = new Post(post.date, post.title, post.body, post.author, post.organization);
                post.setMember(author);
                posts.save(post);
                postContainer.setPostList(getAllPostsByAuthor(author));
                System.out.println("post id = " + post.id);
            }
        } catch (Exception ex){
            postContainer.setErrorMessage("An exception occurred creating a post");
            ex.printStackTrace();
        }
        return postContainer;
    }

    @RequestMapping(path = "/memberList.json", method = RequestMethod.GET)
    public List<Member> getMemberList() {
        List<Member> memberList = new ArrayList<>();
        Iterable <Member> allMembers = members.findAll();
        for (Member member : allMembers) {
            memberList.add(member);
        }
        return memberList;
    }

    public List<Post> getAllPostsByAuthor(Member author) {
        Iterable<Post> allPosts = posts.findByAuthor(author);
        List<Post> postList = new ArrayList<>();
        for (Post currentPost : allPosts) {
            postList.add(currentPost);

        }
        System.out.println("after iterable");
        return postList;
    }

    @RequestMapping(path = "/postsListByMember.json", method = RequestMethod.POST)
    public PostContainer getAllPostsByAuthorWithEndpoint(@RequestBody Member author) {
//        author = (Member) session.getAttribute("member");
        author = members.findFirstByEmail(author.getEmail());
        System.out.println("Author is: " + " " + author.getFirstName());
        PostContainer postContainer = new PostContainer();
        Iterable<Post> allPosts = posts.findByAuthor(author);
        System.out.println("Iterable created");
        List<Post> postList = new ArrayList<>();
        System.out.println("ArrayList created");
        for (Post currentPost : allPosts) {
            System.out.println("inside of for loop");
            postList.add(currentPost);
            try {
                if (postList == null) {
                    postContainer.setErrorMessage("Post list was empty and therefore cannot be saved");

                } else {
                    postContainer.setPostList(postList);
                    System.out.println("post id = " + postList.indexOf(currentPost));
                }
            } catch (Exception ex){
                postContainer.setErrorMessage("An exception occurred creating a post list");
                ex.printStackTrace();
            }
        }
        System.out.println("after iterable");
        return postContainer;
    }

//    @RequestMapping(path = "/postsListByMember.json", method = RequestMethod.GET)
//    public PostContainer getAllPostsByAuthorWithEndpointGet(HttpSession session, Member author) {
//        author = (Member) session.getAttribute("member");
//        PostContainer postContainer = new PostContainer();
//        Iterable<Post> allPosts = posts.findByAuthor(author);
//        List<Post> postList = new ArrayList<>();
//        for (Post currentPost : allPosts) {
//            postList.add(currentPost);
//            try {
//                if (postList == null) {
//                    postContainer.setErrorMessage("Post list was empty and therefore cannot be saved");
//
//                } else {
//                    postContainer.setPostList(postList);
//                    System.out.println("post id = " + postList.indexOf(currentPost));
//                }
//            } catch (Exception ex){
//                postContainer.setErrorMessage("An exception occurred creating a post list");
//                ex.printStackTrace();
//            }
//        }
//        System.out.println("after iterable");
//        return postContainer;
//    }

    @RequestMapping(path = "/postsList.json", method = RequestMethod.GET)
    public List<Post> getAllPosts() {
        Iterable<Post> allPosts = posts.findAll();
        List<Post> postList = new ArrayList<>();
        for (Post currentPost : allPosts) {
            postList.add(currentPost);
        }
        return postList;
    }

    //test with angular

    @RequestMapping(path = "/editPost.json", method = RequestMethod.POST)
    public PostContainer editPost(HttpSession session, @RequestBody Post thispost) {
        Member author = (Member) session.getAttribute("author");
        PostContainer myResponse = new PostContainer();
        try {
            if (author == (thispost.author)) {

                posts.save(thispost);

                System.out.println("Saving edited post");

                myResponse.postList = posts.findByAuthor(author);
                System.out.println("Returning list of posts by  author");
            } else {
                myResponse.errorMessage = "Member did not create post and thus cannot edit it.";
            }
        } catch (Exception ex){
            myResponse.errorMessage = "An Error occurred while editing a post";
            ex.printStackTrace();
        }
        return myResponse;
    }

    @RequestMapping(path = "/singlePost.json", method = RequestMethod.GET)
    public PostContainer getSpecificPost(Integer postID) {
        System.out.println("finding post with post id " + postID);
        PostContainer myResponse = new PostContainer();

        Post myPost = posts.findById(postID);
        try {
            if (myPost == null) {
                myResponse.setErrorMessage("No post found");
            } else {
                System.out.println("Found post with title:" + myPost.title);
                myResponse.setResponsePost(myPost);
            }
        } catch (Exception ex){
            myResponse.setErrorMessage("Exception while getting single post");
            ex.printStackTrace();
        }
        return myResponse;
    }


    @RequestMapping(path = "/createEvent.json", method = RequestMethod.POST)
    public EventContainer createEvent(HttpSession session, @RequestBody Event thisEvent) {
        Member member = (Member) session.getAttribute("member");
        EventContainer myResponse = new EventContainer();
        thisEvent = new Event(thisEvent.name, thisEvent.date, thisEvent.location, thisEvent.information);

        try{
            if(thisEvent == null) {
               myResponse.setErrorMessage("Retrieved a null event");

            } else {
                thisEvent = new Event(thisEvent.name,thisEvent.date, thisEvent.location, thisEvent.information, thisEvent.organizer, thisEvent.organization);
                thisEvent.setOrganizer(member);
                events.save(thisEvent);

                System.out.println("Creating event");
                myResponse.setEventList(getAllEvents());
                System.out.println("Returning list of events");
            }
        } catch (Exception ex){
            myResponse.setErrorMessage("An Error occurred while creating an event");
            ex.printStackTrace();
        }
        return myResponse;
    }


    @RequestMapping(path = "/editEvent.json", method = RequestMethod.POST)
    public EventContainer editEvent(HttpSession session, @RequestBody Event thisEvent) {
        Member member = (Member) session.getAttribute("member");
        EventContainer myResponse = new EventContainer();
        try {
            if (member == (thisEvent.organizer)) {

                events.save(thisEvent);

                System.out.println("Saving edited event");

                myResponse.setEventList(getAllEvents());
                System.out.println("Returning list of events");
            } else {
                myResponse.setErrorMessage("Member did not create event and thus cannot edit it.");
            }
        } catch (Exception ex){
            myResponse.setErrorMessage("An Error occurred while editing an event");
            ex.printStackTrace();
        }
        return myResponse;
    }


    @RequestMapping(path = "/eventsList.json", method = RequestMethod.GET)
    public EventContainer eventThings(HttpSession session) {
        EventContainer myResponse = new EventContainer();
        ArrayList<Event> myEvents = getAllEvents();
        int myEventListSize = myEvents.size();

        if (myEventListSize == 0) {
            myResponse.setErrorMessage("No events to display");

        } else {
            myResponse.setEventList(myEvents);
//            for (Event myEvent : myEvents) {
//                myResponse.eventList.add(myEvent);
//                System.out.println("returning list of events");
//            }
        }
        return myResponse;
    }

    ArrayList<Event> getAllEvents() {
        ArrayList<Event> eventList = new ArrayList<Event>();
        Iterable<Event> allEvents = events.findAll();

        for (Event currentEvent : allEvents) {
            eventList.add(currentEvent);

        }
        return eventList;
    }

    @RequestMapping(path = "/eventsListByMember.json", method = RequestMethod.GET)
    public PostContainer getAllEventsByAuthorWithEndpointGet(HttpSession session, Member author) {
        author = (Member) session.getAttribute("member");
        PostContainer postContainer = new PostContainer();
        Iterable<Post> allPosts = posts.findByAuthor(author);
        List<Post> postList = new ArrayList<>();
        for (Post currentPost : allPosts) {
            postList.add(currentPost);
            try {
                if (postList == null) {
                    postContainer.setErrorMessage("Post list was empty and therefore cannot be saved");

                } else {
                    postContainer.setPostList(postList);
                    System.out.println("post id = " + postList.indexOf(currentPost));
                }
            } catch (Exception ex){
                postContainer.setErrorMessage("An exception occurred creating a post list");
                ex.printStackTrace();
            }
        }
        System.out.println("after iterable");
        return postContainer;
    }

    @RequestMapping(path = "/event.json", method = RequestMethod.GET)
    public EventContainer getSpecificEvent(Integer eventID) {
        System.out.println("finding event with event id " + eventID);
        EventContainer myResponse = new EventContainer();
        Event myEvent = events.findById(eventID);
        try {
            if (myEvent == null) {
                myResponse.setErrorMessage("No event found");
            } else {
                System.out.println("Found event " + myEvent.name);
                myResponse.setResponseEvent(myEvent);
            }
        } catch (Exception ex){
            myResponse.setErrorMessage("An exception occurred while retrieving event. ");
            ex.printStackTrace();
        }
        return myResponse;
    }

    @RequestMapping(path = "/attendEvent.json", method = RequestMethod.POST)
    public MemberEventContainer checkInAtEvent(HttpSession session, @RequestBody Event event) throws Exception{
        MemberEventContainer myResponse = new MemberEventContainer();
        Member member = (Member) session.getAttribute("member");

        try {
            MemberEvent attendingEvent = new MemberEvent(member, event);

            memberevents.save(attendingEvent);

            myResponse.setEventList(memberevents.findMembersByEvent(event));

        } catch (Exception ex){
            myResponse.setErrorMessage("A problem occurred while trying to attend an event");
            ex.printStackTrace();
        }
        return myResponse;
    }


    @RequestMapping(path = "/sendInvitation.json", method = RequestMethod.POST)
    public InvitationContainer evite(HttpSession session, @RequestBody String invitedEmail) throws Exception {
        InvitationContainer myResponse = new InvitationContainer();
        Member member = (Member) session.getAttribute("member");
        try{
            if (invitedEmail == null){
                myResponse.setErrorMessage("Invited email was null");
            } else {
            myResponse.setSuccessMessage("Invitation sent successfully");
            }
        } catch (Exception ex) {
            myResponse.setErrorMessage("An error occurred while trying to send an invite");
            ex.printStackTrace();
        }
        return myResponse;
    }

    @RequestMapping (path= "/createOrganization.json", method = RequestMethod.POST)
    public OrganizationContainer createOrganization(HttpSession session, @RequestBody Organization organization) throws  Exception {
        Member member = (Member) session.getAttribute("member");
        OrganizationContainer organizationContainer = new OrganizationContainer();
        organization = new Organization(organization.name);
        try {
            if (organization == null) {
                organizationContainer.setErrorMessage("Organization name was empty and therefore cannot be saved");

            } else {
                organization = new Organization(organization.name);
                organizations.save(organization);
                organizationContainer.setResponseOrganization(organization);
                OrganizationMember newOrgMember = new OrganizationMember(organization, member);
                System.out.println("Organization id = " + organization.id);
            }
        } catch (Exception ex){
            organizationContainer.setErrorMessage("An exception occurred creating an organization");
            ex.printStackTrace();
        }
        return organizationContainer;
    }

    @RequestMapping (path= "/organizationProfile.json", method = RequestMethod.GET)
    public OrganizationContainer thisOrg(HttpSession session, @RequestBody Integer organizationId) throws Exception {
        OrganizationContainer myResponse = new OrganizationContainer();
        Organization myOrg = organizations.findOne(organizationId);
        try{
            if (myOrg == null){
                myResponse.setErrorMessage("Organization was null");
            } else {
                myResponse.setResponseOrganization(myOrg);
            }

        } catch (Exception ex){
            myResponse.setErrorMessage("Exception while accessing org profile");
            ex.printStackTrace();
        }
        return myResponse;
    }

//    @RequestMapping (path= "/joinOrganization.json", method = RequestMethod.POST)
//    public OrganizationMemberContainer joinOrganization(HttpSession session, @RequestBody Integer organizationId) throws Exception {
//        OrganizationMemberContainer myResponse = new OrganizationMemberContainer();
//        Member member = (Member) session.getAttribute("member");
//        Organization organization = organizations.findOne(organizationId);
//
//        try {
//            if(member.email.equals(invitations.findByInvitedEmail(member.getEmail()))) {
//                OrganizationMember organizationMemberAssociation = new OrganizationMember(organization, member);
//                organizationMemberAssociation.setOrganization(organization);
//                organizationMembers.save(organizationMemberAssociation);
//                myResponse.setOrganizationMemberList(organizationMembers.findMembersByOrganization(organization));
//                System.out.println("organization set");
//            } else {
//                myResponse.setErrorMessage("User was not invited to join this organization");
//            }
//        } catch (Exception ex) {
//            myResponse.setErrorMessage("A problem occurred while trying to join an organization");
//            ex.printStackTrace();
//        }
//        return myResponse;
//    }

    @RequestMapping (path= "/joinOrganization.json", method = RequestMethod.POST)
    public OrganizationMemberContainer joinOrganization(HttpSession session) throws Exception {
        OrganizationMemberContainer myResponse = new OrganizationMemberContainer();
        Member member = (Member) session.getAttribute("member");
        ArrayList<Invitation> allInvites =  invitations.findByInvitedEmail(member.getEmail());

        try {
            if(allInvites != null) {
                for (Invitation currentInvite: allInvites) {
                    Organization organization = currentInvite.getOrganization();
                    OrganizationMember organizationMemberAssociation = new OrganizationMember(organization, member);
                    organizationMemberAssociation.setOrganization(organization);
                    organizationMembers.save(organizationMemberAssociation);
                    myResponse.setOrganizationMemberList(organizationMembers.findMembersByOrganization(organization));
                    System.out.println("organization set");
                }
            } else {
                myResponse.setErrorMessage("User was not invited to join this organization");
            }
        } catch (Exception ex) {
            myResponse.setErrorMessage("A problem occurred while trying to join an organization");
            ex.printStackTrace();
        }
        return myResponse;
    }


    public ArrayList<OrganizationMember> refreshOrganizationMemberList() {
        ArrayList<OrganizationMember> organizationMembersArrayList = new ArrayList<>();
        Iterable<OrganizationMember> allOrganizationMembers = organizationMembers.findAll();

        for (OrganizationMember orgMem : allOrganizationMembers) {
            organizationMembersArrayList.add(orgMem);

        }
        return organizationMembersArrayList;
    }

    @RequestMapping (path= "/memberProfile.json", method = RequestMethod.GET)
    public MemberResponseContainer thisMember(HttpSession session, @RequestBody Integer memberId) throws Exception {
        MemberResponseContainer myResponse = new MemberResponseContainer();
        Member myMember = members.findOne(memberId);
        try{
            if (myMember == null){
                myResponse.setErrorMessage("Member was null");
            } else {
                myResponse.setResponseMember(myMember);
            }

        } catch (Exception ex){
            myResponse.setErrorMessage("Exception while accessing member profile");
            ex.printStackTrace();
        }
        return myResponse;
    }

    @RequestMapping(path = "/organizationsList.json", method = RequestMethod.GET)
    public List<Organization> getAllOrganizations() {
        Iterable<Organization> allOrganizations = organizations.findAll();
        List<Organization> organizationsList = new ArrayList<>();
        for (Organization thisOrganization : allOrganizations) {
            organizationsList.add(thisOrganization);
        }
        return organizationsList;
    }


    @RequestMapping (path= "/postsByOrg.json", method = RequestMethod.POST)
    public PostContainer getAllPosts(HttpSession session, @RequestBody Organization organization){
        PostContainer myResponse = new PostContainer();
        try {
            ArrayList<Post> postsByOrg = new ArrayList<>();
            postsByOrg= posts.findByOrganization(organization);
            if (postsByOrg == null){
                myResponse.setErrorMessage("This organization has no posts");
            } else {
                myResponse.setPostList(postsByOrg);
            }
        }catch (Exception ex){
            myResponse.setErrorMessage("An exception occurred in getting posts by organization");
            ex.printStackTrace();
        }
        return myResponse;
    }

    @RequestMapping (path= "/eventsByOrg.json", method = RequestMethod.POST)
    public EventContainer getAllEvents(HttpSession session, @RequestBody Organization organization){
        EventContainer myResponse = new EventContainer();
        try {
            ArrayList<Event> eventsByOrg = new ArrayList<>();
            eventsByOrg = events.findByOrganization(organization);
            if (eventsByOrg == null){
                myResponse.setErrorMessage("This organization has no events");
            } else {
                myResponse.setEventList(eventsByOrg);
            }
        } catch (Exception ex){
            myResponse.setErrorMessage("An exception occurred in getting events by organization");
            ex.printStackTrace();
        }
        return myResponse;
    }
}
