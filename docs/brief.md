# Help Desk · Level 1: Routing the queue

A three-person team builds the core of a support team's help desk on a project that already exists. You decide how to split the work, you agree on the details with product, and you prove it works.

Team of 3 · as many sessions as it takes · ends in a team demo

## What you're getting

A working Spring Boot project in a shared GitHub repo. It already has tickets and agents, a fixed list of support agents, and a way to create and read tickets. Read it and get it running on your machine before you start building. Steps 1 to 5 below need no code, so setup can happen alongside them.

It also has a script that puts the database back to its starting state (no tickets, only the seed agents), so you can get back to a clean start whenever you need one. The README explains how to run, reset and test it. The project's own tests run against a separate, throwaway database, so they never touch your data.

Treat it like any codebase you join: it has an existing style, and you can change anything in it if you have a reason.

## The problem

> From product: Our support team gets tickets faster than anyone can sort them by hand. Right now a lead reads every new ticket and picks someone to give it to, and tickets get lost when everyone's busy. Each ticket has a priority. Each agent can only handle so many open tickets at once. We need the help desk to hand out work on its own, let agents work tickets through to done, and catch tickets that have been sitting too long, before the customer has to chase us.

## What product needs

These are product's acceptance criteria, written from the side of the people who'll use the help desk. They're a starting point, not the full answer.

**Support leads need:**

1. New tickets handed out to an agent with room for them, without anyone choosing.
2. Nothing lost when everyone's busy: if every agent is full, a new ticket waits until someone has room.
3. Tickets that have gone too long without being resolved escalated: their priority goes up, and the team can see they were escalated.
4. A way to show escalation working without waiting hours for a ticket to become overdue.

**Agents need:**

5. To see the tickets they have, and which ones they've started.
6. To work a ticket through to resolved.
7. Once they resolve a ticket, it stops counting against them, and if anything is waiting, they get the next one.

Product hasn't thought of everything. Where something isn't clear, ask.

## How you work

These steps come in order, but they aren't phases with deadlines. Once a step is done, move on. There's no set number of sessions. Keep going until your agreed criteria pass, then demo.

### 1. Ask product

Your facilitator plays product. Ask product your questions with the whole team there, so everyone builds on the same answers. Write every answer down in `docs/product-answers.md`. Product answers what the business needs. Product doesn't make technical decisions: the endpoints, the data model and the code structure are yours.

You can keep asking questions at any point, not only at the start.

### 2. Agree on the acceptance criteria

Write your own version of the acceptance criteria: precise enough that anyone could tell whether the system meets them. Put it in `docs/acceptance-criteria.md` and open a pull request. Product signs it off by approving that pull request. After that it changes only through a new pull request that product approves, so if you find a gap while building, raise it with product instead of quietly working around it.

### 3. Split the work

Decide as a team how to break the work into pieces. There's no right number of pieces and no single right split. Different teams find different good ones. Every person owns at least one piece and demos it. A good split follows four rules:

- **Split by what the system does, not by layer.** "Controllers," "the database" and "tests" aren't pieces. Nobody could demo them on their own.
- **Each piece can be demoed on its own.** Its owner can show it doing something real.
- **Nobody waits to start.** Each owner can begin building before anyone else merges anything.
- **Every place two pieces meet has one named owner.** When one piece needs something to happen in another, the team has agreed whose code makes it happen.

Write down the pieces, who owns each, and where they meet in `docs/design.md`, open a pull request, and walk your facilitator through it. They'll check it against these four rules, not against an answer.

### 4. Agree how your pieces meet

Where two pieces meet, their owners agree on the details before building against each other: the endpoints, the shared data, and who triggers what. Add it to `docs/design.md`.

### 5. Break it into issues

Each owner breaks their pieces into GitHub issues in the repo: small units of work, each with what it does and how you'll know it's done. The repo's issue template has those two headings. Teammates read each other's issues before building, because that's often where a missed seam shows up.

### 6. Build

From here on, every pull request closes an issue (write "Closes #12" in its description). Nobody pushes to `main`. Every change goes through a pull request, and both teammates approve it before it merges. A new push resets the approvals. You can work outside sessions, but only through pull requests, and nobody merges changes into a piece someone else owns.

**Finished early?** Help a teammate by reviewing their pull requests or pairing with them, but the owner writes their piece.

**AI:** keep it to a bare minimum. Ask it questions, don't have it write code. This is on the honor system.

### 7. Prove it works

Build a Postman collection (or automated tests) with one scenario for each agreed criterion, and make it pass against `main`. You'll run it live in the demo. Keep it: it's how you'll know nothing broke when this project grows.

### 8. Demo

Each person demos:

- what your pieces do
- one real scenario, saying what should happen before you show it
- the edge cases you found
- one decision you made and why
- anything you'd still change

Then run the team's collection live. Peers give one thing that was clear and one thing they had to guess.

## What feedback focuses on

- The questions you asked product, and what you did with the answers
- Edge cases handled, and correct HTTP responses
- How your pieces fit together, and how you agreed on it
- Issues, pull requests and reviews that a teammate can follow
- A clear demo with one explained trade-off

No scores, no winners. Build it like you'll have to live with it, because this project keeps growing after Level 1.
