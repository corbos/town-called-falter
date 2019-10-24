; (function () {

    const socketUrl = "ws://localhost:8080/messages"

    let ws;
    let storage = window.sessionStorage;

    function byId(id) {
        return document.getElementById(id);
    }

    let divHome = byId("divHome");
    let divCreate = byId("divCreate");
    let divJoin = byId("divJoin");
    let divPlay = byId("divPlay");
    let divRoster = byId("divRoster");
    let divStartSetup = byId("divStartSetup");
    let divStart = byId("divStart");
    let divErr = byId("divErr");
    let ulErrors = byId("ulErrors");

    let txtModeratorName = byId("txtModeratorName");
    let txtPlayerName = byId("txtPlayerName");
    let txtGameCode = byId("txtGameCode");

    let spnGameCode = byId("spnGameCode");
    let spnPlayerName = byId("spnPlayerName");

    let gameHeader = byId("gameHeader");

    function hideAll() {
        divHome.classList.add("hidden");
        divCreate.classList.add("hidden");
        divJoin.classList.add("hidden");
        divPlay.classList.add("hidden");
        divErr.classList.add("hidden");
    }

    function isModerator() {
        return storage.getItem("moderator") === "true";
    }

    function hideAllModeratorPanels() {
        divStartSetup.classList.add("hidden");
        divStart.classList.add("hidden");
    }

    function showErr(errors) {
        let html = "";
        if (typeof errors === "string") {
            html = `<li>${errors}</li>`;
        } else if (errors.constructor === Array) {
            errors.forEach(function (err) {
                html += `<li>${err}</li>`;
            });
        } else {
            console.log(errors);
            html = "<li>Unknown Error.</li>";
        }

        ulErrors.innerHTML = html;

        divErr.classList.remove("hidden");
    }

    function handleNetworkErr(err) {
        console.log(err);
    }

    function render(msg) {

        hideAllModeratorPanels();
        let moderator = isModerator();
        let arrange = false;
        let kill = false;

        switch (msg.gameStatus) {
            case "JOINABLE":
                gameHeader.textContent = "Waiting for others to join...";
                if (moderator) {
                    divStartSetup.classList.remove("hidden");
                }
                break;
            case "SETUP":
                gameHeader.textContent = "Moderator is setting up.";
                if (moderator) {
                    divStart.classList.remove("hidden");
                    arrange = true;
                }
                break;
            case "DAY":
                gameHeader.textContent = "Day";
                document.body.classList.add("day");
                document.body.classList.remove("night");
                kill = moderator;
                break;
            case "NIGHT":
                gameHeader.textContent = "Night";
                document.body.classList.add("night");
                document.body.classList.remove("day");
                kill = moderator;
                break;
            case "EVIL_WINS":
                gameHeader.textContent = "EVIL WINS.";
                break;
            case "GOOD_WINS":
                gameHeader.textContent = "GOOD WINS.";
                break;
            default:
                break;
        }

        let html = "";
        msg.players.forEach(function (player) {

            let controls = "";

            if (arrange) {
                controls = `<div>
                <a href="#nowhere" class="href-btn" onclick="return routeClick(this, 'movePlayerUp');">
                    <img src="/images/up.svg" class="connection-status" alt="move player up" title="move player up">
                </a>
                </div>`;
            } else if (kill && player.status === "ALIVE") {
                controls = `<div>
                <a href="#nowhere" class="href-btn" onclick="return routeClick(this, 'kill');">
                    kill :(
                </a>
                </div>`;
            }

            html += `<div class="player">
                <div>${player.name}</div>
                <div>
                    <img src="/images/${player.connected ? "connected.svg" : "disconnected.svg"}" class="connection-status">
                </div>
                <div>${player.status}</div>
                ${controls}
            </div>`;
        });
        divRoster.innerHTML = html;
    }

    function ack(msg) {

        storage.setItem("playerName", msg.playerName);
        storage.setItem("gameCode", msg.gameCode);
        storage.setItem("moderator", msg.moderator);

        spnGameCode.textContent = msg.gameCode;
        spnPlayerName.textContent = msg.playerName;

        hideAll();
        divPlay.classList.remove("hidden");
    }

    function connect(playerName, gameCode) {

        ws = new WebSocket(socketUrl);

        ws.onopen = function () {
            let msg = {
                playerName: playerName,
                gameCode: gameCode
            }
            ws.send(JSON.stringify(msg));
        };

        ws.onmessage = function (message) {
            let msg = JSON.parse(message.data);
            console.log(msg);
            switch (msg.type) {
                case "ACK":
                    ack(msg);
                    break;
                case "GAME_STATE":
                    render(msg);
                    break;
            }
        };

        ws.onclose = function () {
            console.log("ws closed.");
        };

        ws.onerror = function (err) {
            console.log("ws err:", err);
        };
    }

    function checkSession() {
        if (storage.getItem("gameCode")) {
            connect(storage.getItem("playerName"), storage.getItem("gameCode"));
        }
    }

    var actions = {
        showCreate: function () {
            divHome.classList.add("hidden");
            divCreate.classList.remove("hidden");
        },
        showJoin: function () {
            divHome.classList.add("hidden");
            divJoin.classList.remove("hidden");
        },
        cancel: function () {
            hideAll();
            divHome.classList.remove("hidden");
        },
        create: function (evt) {

            evt.target.setAttribute("disabled", true);

            let moderatorName = txtModeratorName.value.trim();
            if (moderatorName.length === 0) {
                evt.target.removeAttribute("disabled");
                showErr("Moderator name is required.");
                return;
            }

            let config = {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    playerName: moderatorName
                })
            };

            fetch("/create", config)
                .then(function (resp) {
                    resp.json().then(function (msg) {
                        if (msg.status === "SUCCESS") {
                            connect(moderatorName, msg.value.code);
                        } else if (msg.status === "INVALID") {
                            showErr(msg.errors);
                        } else {
                            showErr(msg);
                        }
                    });
                })
                .catch(handleNetworkErr)
                .finally(function () {
                    evt.target.removeAttribute("disabled");
                });
        },
        join: function (evt) {

            evt.target.setAttribute("disabled", true);

            let errors = [];

            let playerName = txtPlayerName.value.trim();
            if (playerName.length === 0) {
                errors.push("Player name is required.")
            }

            let gameCode = txtGameCode.value.trim();
            if (gameCode.length === 0) {
                errors.push("Game code is required.")
            }

            if (errors.length > 0) {
                evt.target.removeAttribute("disabled");
                showErr(errors);
                return;
            }

            let config = {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    playerName: playerName,
                    gameCode: gameCode
                })
            };

            fetch("/join", config)
                .then(function (resp) {
                    resp.json().then(function (msg) {
                        if (msg.status === "SUCCESS") {
                            connect(playerName, gameCode);
                        } else if (msg.status === "INVALID") {
                            // we show the error, but then try to reconnect
                            showErr(msg.errors);
                            connect(playerName, gameCode);
                        } else if (msg.status === "NOT_FOUND") {
                            showErr(msg.errors);
                        } else {
                            showErr(msg);
                        }
                    });
                })
                .catch(handleNetworkErr)
                .finally(function () {
                    evt.target.removeAttribute("disabled");
                });
        },
        startSetup: function () {
            let msg = {
                playerName: storage.getItem("playerName"),
                gameCode: storage.getItem("gameCode"),
                move: {
                    type: "SETUP"
                }
            }
            ws.send(JSON.stringify(msg));
        },
        start: function () {

            let orderedPlayers = [];

            for (let i = 0; i < divRoster.childNodes.length; i++) {
                let node = divRoster.childNodes[i].firstChild;
                while (node.nodeType !== 1) { // find non-text node
                    node = node.nextSibling;
                }
                orderedPlayers.push(node.textContent.trim());
            }

            let msg = {
                playerName: storage.getItem("playerName"),
                gameCode: storage.getItem("gameCode"),
                move: {
                    type: "START",
                    names: orderedPlayers
                }
            }

            ws.send(JSON.stringify(msg));
        }
    };

    let clickHandlers = {
        movePlayerUp: function (href) {
            let div = href.parentElement.parentElement;
            div.parentElement.insertBefore(div, div.previousSibling);
        },
        kill: function (href) {

            let div = href.parentElement.parentElement;
            let node = div.firstChild;
            while (node.nodeType !== 1) { // find non-text node
                node = node.nextSibling;
            }

            console.log(node.textContent.trim());

            let msg = {
                playerName: storage.getItem("playerName"),
                gameCode: storage.getItem("gameCode"),
                move: {
                    type: "KILL",
                    names: [node.textContent.trim()]
                }
            }

            ws.send(JSON.stringify(msg));
        }
    };

    window.routeClick = function (elem, name) {
        clickHandlers[name](elem);
        return false;
    };

    // process DOM and check for state
    function handleAction(evt) {
        let action = this.getAttribute("data-action");
        actions[action].call(this, evt);
    }

    let clickables = document.querySelectorAll("*[data-action]");
    for (let i = 0; i < clickables.length; i++) {
        clickables[i].addEventListener("click", handleAction);
    }

    checkSession();

})();