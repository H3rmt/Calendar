# Calendar

[![Gradle Build Main](https://github.com/H3rmt/Calendar/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/H3rmt/Calendar/actions/workflows/build.yml)
[![Gradle Build Next](https://github.com/H3rmt/Calendar/actions/workflows/build.yml/badge.svg?branch=next)](https://github.com/H3rmt/Calendar/actions/workflows/build.yml)
[![Scan with Detekt](https://github.com/H3rmt/Calendar/actions/workflows/detekt.yml/badge.svg?branch=main)](https://github.com/H3rmt/Calendar/actions/workflows/detekt.yml)

A desktop calendar written in [Kotlin](https://github.com/edvin/tornadofx) with [Tornadofx](https://github.com/edvin/tornadofx) with appointments, notes and reminders


## Features
### Overview
- Overview of all days in one month
- Buttons to quickly create reminder and notes for specific day
- Overview of all appointments for each day and whole week
![Overview](img/Overview.png)

### Appointments
- Each appointment has a specific type like school, work, sports, etc. 
- appointments can cover multiple days or weeks
- Appointment consists of a title a description
![Appointments](img/appointments.png)


### Notes
- Notes can be created for specific day or week
- Each day/week can have one note for each type 
- Notes contain a complete texteditor to write rich text 
![Notes](img/Notes.png)

### Reminder
- Reminder can have an appointment as their deadline or a simple date
- Feature a title and a description like an appointment
![Reminder](img/Reminder.png)


## TODO
- Reminder without deadline
- Settingstab
  - Settings for different typs
  - Settings for Weekly Appointments
- Notifications
- Improved Reminder Overview
- Global notes
- Notes overview
- Global TODOs
