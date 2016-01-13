- WC: Wifi Connected
- SO: Screen On
- AN: Active Notification
- DT: Discovery With Timeout
- DN: Discovery No Timeout
- SD: Stop Discovery
- RT: Remove Timeout
- X: Do nothing

| Event | State               | Result |
|-------|---------------------|--------|
| AN    | SO && WC            | DT     |
| AN    | SO && !WC           | X      |
| AN    | !SO && WC           | DN     |
| AN    | !SO && !WC          | X      |
|       |                     |        |
| !AN   | SO && WC            | SD     |
| !AN   | SO && !WC           | SD     |
| !AN   | !SO && WC           | SD     |
| !AN   | !SO && !WC          | SD     |
|       |                     |        |
| SO    | AN && WC            | DT     |
| SO    | AN && !WC           | X      |
| SO    | !AN && WC           | X      |
| SO    | !AN && !WC          | X      |
|       |                     |        |
| !SO   | AN && WC            | RT     |
| !SO   | AN && !WC           | X      |
| !SO   | !AN && WC           | X      |
| !SO   | !AN && !WC          | X      |
|       |                     |        |
| WC    | AN && SO            | DT     |
| WC    | AN && !SO           | DN     |
| WC    | !AN && SO           | X      |
| WC    | !AN && !SO          | X      |
|       |                     |        |
| !WC   | AN && SO            | SD     |
| !WC   | AN && !SO           | SD     |
| !WC   | !AN && SO           | X      |
| !WC   | !AN && !SO          | X      |
|       |                     |        |
| TO    | AN && WC && SO      | SA     |
| TO    | AN && WC && !SO     | SA     |
| TO    | AN && !WC && SO     | X      |
| TO    | AN && !WC && !SO    | X      |
| TO    | !AN && WC && SO  SA | X      |
| TO    | !AN && WC && !SO    | X      |
| TO    | !AN && !WC && SO    | X      |
| TO    | !AN && !WC && !SO   | X      |
