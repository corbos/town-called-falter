; (function () {

    const socketUrl = `ws://${window.location.host}/messages`;

    let ws;
    let storage = window.sessionStorage;

    function byId(id) {
        return document.getElementById(id);
    }

    let divHome = byId("divHome");
    let divCreate = byId("divCreate");
    let divJoin = byId("divJoin");
    let divPlay = byId("divPlay");
    let divRole = byId("divRole");
    let divRoster = byId("divRoster");
    let divStartSetup = byId("divStartSetup");
    let divStart = byId("divStart");
    let divErr = byId("divErr");
    let divNominate = byId("divNominate");
    let divVote = byId("divVote");
    let divMessages = byId("divMessages");

    let ulErrors = byId("ulErrors");

    let txtModeratorName = byId("txtModeratorName");
    let txtPlayerName = byId("txtPlayerName");
    let txtGameCode = byId("txtGameCode");

    let spnGameCode = byId("spnGameCode");
    let spnPlayerName = byId("spnPlayerName");

    let gameHeader = byId("gameHeader");

    // action panel
    let actionPanel = {
        prompt: null,
        panel: byId("divAction"),
        promptHeader: byId("promptHeader"),
        divActionPlayers: byId("divActionPlayers"),
        hide: function () {
            this.panel.classList.add("hidden");
        },
        getSelectedPlayers: function () {
            let json = storage.getItem("selectedPlayers");
            if (json && json.length > 0) {
                return JSON.parse(json);
            }
            return [];
        },
        setSelectedPlayers: function (selectedPlayers) {
            storage.setItem("selectedPlayers", JSON.stringify(selectedPlayers));
        },
        init: function (prompt, players) {

            if (prompt) {
                if (!this.areEqual(prompt)) {
                    this.promptHeader.textContent = prompt.prompt;
                    let selectedPlayers = this.getSelectedPlayers();
                    let html = "";
                    players.forEach(function (p) {
                        let selected = selectedPlayers.indexOf(p.name) >= 0 ? 'class="selected"' : '';
                        html += `<div>
                        <button onclick="routeClick(this, 'select');"${selected}>${p.name}</button>
                    </div>`;
                    });
                    this.divActionPlayers.innerHTML = html;
                }
                this.panel.classList.remove("hidden");
            } else {
                this.hide();
            }
        },
        areEqual: function (prompt) {

            let eq = (prompt === null && this.prompt === null)
                || (prompt !== null && this.prompt !== null
                    && prompt.version === this.prompt.version);

            if (!eq) {
                this.prompt = prompt;
            }

            return eq;
        },
        select: function (btn) {

            let selectedPlayers = this.getSelectedPlayers();
            let playerName = btn.innerText.trim();

            if (btn.classList.contains("selected")) {
                let index = selectedPlayers.indexOf(playerName);
                selectedPlayers.splice(index, 1);
                btn.classList.remove("selected");
            } else {
                btn.classList.add("selected");
                selectedPlayers.push(playerName);
            }

            if (selectedPlayers.length === this.prompt.count) {

                let msg = {
                    playerName: storage.getItem("playerName"),
                    gameCode: storage.getItem("gameCode"),
                    move: {
                        type: "USE_ABILITY",
                        ability: this.prompt.ability,
                        names: selectedPlayers
                    }
                }

                ws.send(JSON.stringify(msg));

                // clear
                selectedPlayers = [];
                this.prompt = null;
            }

            this.setSelectedPlayers(selectedPlayers);
        }
    };

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
        showErr(err);
    }

    function renderRole(role) {
        divRole.innerHTML = `<div><label>Role:</label>${role.name}</div>
        <p>${role.description}</p>`;
        divRole.classList.remove("hidden");
    }

    function renderVote(msg) {
        if (msg.canVote) {
            byId("divVoteMessage").innerHTML = `<div>${msg.nominator} nominated ${msg.nominated}.</div>
            <div>Execute ${msg.nominated}?</div>`;
            divVote.classList.remove("hidden");
        }
    }

    function renderMessages(playerMessages) {
        if (playerMessages.length > 0) {
            let html = '<h3>Private Messages</h3>';
            playerMessages.forEach(function (msg) {
                html += `<div>${msg}</div>`
            });
            divMessages.innerHTML = html;
            divMessages.classList.remove("hidden");
        }
    }

    function render(gameState) {

        // hide stuff. gotta be a better way
        divStartSetup.classList.add("hidden");
        divStart.classList.add("hidden");
        divNominate.classList.add("hidden");
        divVote.classList.add("hidden");
        divRoster.classList.add("hidden");
        actionPanel.hide();

        let moderator = isModerator();
        let arrange = false;
        let kill = false;
        let canNominate = false;

        switch (gameState.gameStatus) {
            case "JOINABLE":
                gameHeader.textContent = "Waiting for others to join...";
                divRoster.classList.remove("hidden");
                if (moderator) {
                    divStartSetup.classList.remove("hidden");
                }
                break;
            case "SETUP":
                gameHeader.textContent = "Moderator is setting up.";
                divRoster.classList.remove("hidden");
                if (moderator) {
                    divStart.classList.remove("hidden");
                    arrange = true;
                }
                break;
            case "DAY_NOMINATE":
                renderRole(gameState.role);
                renderMessages(gameState.playerMessages);
                gameHeader.textContent = "Day: Accepting Nominations";
                divRoster.classList.remove("hidden");
                document.body.className = "day";
                kill = moderator;
                canNominate = gameState.canNominate;
                if (canNominate) {
                    divNominate.classList.remove("hidden");
                }
                break;
            case "DAY_VOTE":
                renderRole(gameState.role);
                renderMessages(gameState.playerMessages);
                gameHeader.textContent = "Day: Vote!";
                renderVote(gameState);
                document.body.className = "day";
                kill = moderator;
                break;
            case "NIGHT":
                renderRole(gameState.role);
                renderMessages(gameState.playerMessages);
                actionPanel.init(gameState.prompt, gameState.players);
                gameHeader.textContent = "Night";
                divRoster.classList.remove("hidden");
                document.body.className = "night";
                kill = moderator;
                break;
            case "EVIL_WINS":
                renderRole(gameState.role);
                renderMessages(gameState.playerMessages);
                gameHeader.textContent = "EVIL WINS.";
                divRoster.classList.remove("hidden");
                document.body.className = "evil-wins";
                break;
            case "GOOD_WINS":
                renderRole(gameState.role);
                renderMessages(gameState.playerMessages);
                gameHeader.textContent = "GOOD WINS.";
                divRoster.classList.remove("hidden");
                document.body.className = "good-wins";
                break;
            default:
                break;
        }



        let html = "";
        gameState.players.forEach(function (player) {

            let controls = "";

            if (arrange) {
                controls = `<div>
                <a href="#nowhere" class="href-btn" onclick="return routeClick(this, 'movePlayerUp');">
                    <img src="/images/up.svg" class="connection-status" alt="move player up" title="move player up">
                </a>
                </div>`;
            } else if (kill && player.status === "ALIVE") {
                controls = `<div>
                <button onclick="return routeClick(this, 'kill');">kill</button>
                </div>`;
            }

            if (canNominate && storage.getItem("playerName") != player.name
                && gameState.possibleNominations.indexOf(player.name) >= 0) {
                controls += `<div>
                <button onclick="routeClick(this, 'nominate');">nominate</button>
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
            if (msg.type === "ACK") {
                ack(msg);
            } else {
                render(msg);
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
            if (gameCode.length !== 4) {
                errors.push("Game code must be four characters.")
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

            if (!confirm("You can't un-kill someone. Are you sure?")) {
                return;
            }

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
        },
        nominate: function (btn) {

            let div = btn.parentElement.parentElement;
            let node = div.firstChild;
            while (node.nodeType !== 1) { // find non-text node
                node = node.nextSibling;
            }

            console.log(node.textContent.trim());

            let msg = {
                playerName: storage.getItem("playerName"),
                gameCode: storage.getItem("gameCode"),
                move: {
                    type: "NOMINATE",
                    names: [node.textContent.trim()]
                }
            }

            ws.send(JSON.stringify(msg));
        },
        declineNomination: function (btn) {
            let msg = {
                playerName: storage.getItem("playerName"),
                gameCode: storage.getItem("gameCode"),
                move: {
                    type: "NOMINATE",
                    names: []
                }
            }

            ws.send(JSON.stringify(msg));
        },
        select: function (btn) {
            actionPanel.select(btn);
        },
        vote: function (_btn, confirmed) {
            let msg = {
                playerName: storage.getItem("playerName"),
                gameCode: storage.getItem("gameCode"),
                move: {
                    type: "VOTE",
                    confirmed: confirmed
                }
            }

            ws.send(JSON.stringify(msg));
        }
    };

    window.routeClick = function (elem, name, arg) {
        clickHandlers[name](elem, arg);
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