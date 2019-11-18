; (function () {

    const socketUrl = `ws://${window.location.host}/messages`;

    let ws;

    function byId(id) {
        return document.getElementById(id);
    }

    // given a node, finds the first non-text child.
    function firstChildElement(node) {
        let n = node.firstChild;
        while (n.nodeType !== Node.ELEMENT_NODE) {
            n = n.nextSibling;
        }
        return n;
    }

    let divHome = byId("divHome");
    let divCreate = byId("divCreate");
    let divJoin = byId("divJoin");
    let divPlay = byId("divPlay");
    let divRole = byId("divRole");
    let roster = byId("roster");
    let rosterBody = firstChildElement(roster);
    let divStartSetup = byId("divStartSetup");
    let divStart = byId("divStart");
    let divErr = byId("divErr");
    let decline = byId("decline");
    let divVote = byId("divVote");
    let divMessages = byId("divMessages");

    let ulErrors = byId("ulErrors");

    let txtModeratorName = byId("txtModeratorName");
    let txtPlayerName = byId("txtPlayerName");
    let txtGameCode = byId("txtGameCode");

    let spnGameCode = byId("spnGameCode");
    let spnPlayerName = byId("spnPlayerName");

    let gameHeader = byId("gameHeader");
    let promptHeader = byId("promptHeader");

    // session management
    let session = (function () {

        var storage = window.sessionStorage;
        var currentPrompt = null;

        function promptsEqual(value) {
            return (value === null && currentPrompt === null)
                || (value !== null && currentPrompt !== null
                    && value.version === currentPrompt.version);
        }

        return {
            playerName: function (name) {
                if (arguments.length === 0) {
                    return storage.getItem("playerName");
                }
                storage.setItem("playerName", name);
            },
            gameCode: function (code) {
                if (arguments.length === 0) {
                    return storage.getItem("gameCode");
                }
                storage.setItem("gameCode", code);
            },
            moderator: function (mod) {
                if (arguments.length === 0) {
                    return storage.getItem("moderator") === "true";
                }
                storage.setItem("moderator", mod);
            },
            selectedPlayers: function (players) {
                if (arguments.length === 0) {
                    let json = storage.getItem("selectedPlayers");
                    if (json && json.length > 0) {
                        return JSON.parse(json);
                    }
                    return [];
                }
                storage.setItem("selectedPlayers", JSON.stringify(players));
            },
            prompt: function (value) {

                if (arguments.length === 0) {
                    return currentPrompt;
                }

                if (!promptsEqual(value)) {
                    currentPrompt = value;
                }
            }
        };
    })();

    function hideAll() {
        divHome.classList.add("hidden");
        divCreate.classList.add("hidden");
        divJoin.classList.add("hidden");
        divPlay.classList.add("hidden");
        divErr.classList.add("hidden");
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

    function renderSetup(state) {
        if (state.gameStatus === "JOINABLE" && session.moderator()) {
            divStartSetup.classList.remove("hidden");
            if (state.players.length >= 4) {
                var node = firstChildElement(divStartSetup);
                node.removeAttribute("disabled");
                node.setAttribute("title", "Ready to set up!");
            }
        } else {
            divStartSetup.classList.add("hidden");
        }
    }

    function renderStart(state) {
        if (state.gameStatus === "SETUP" && session.moderator()) {
            divStart.classList.remove("hidden");
        } else {
            divStart.classList.add("hidden");
        }
    }

    function renderRole(role) {

        if (role) {

            divRole.innerHTML = `<div><label>Role:</label>${role.name}</div>
                <div>${role.description}</div>`;

            if (role.actualAlignment === "GOOD") {
                divRole.className = "role-good";
            } else if (role.actualAlignment === "EVIL") {
                divRole.className = "role-evil";
            }
        } else {
            roster.className = "hidden";
        }
    }

    function renderVote(state) {
        if (state.canVote) {
            byId("divVoteMessage").innerHTML =
                `<div>${state.nominator} nominated ${state.nominated}.</div>
                <div>Execute ${state.nominated}?</div>`;
            divVote.classList.remove("hidden");
            roster.classList.add("hidden");
        } else {
            roster.classList.remove("hidden");
            divVote.classList.add("hidden");
        }
    }

    function renderMessages(messages) {
        if (messages.length > 0) {
            let html = '<h3>Private Messages</h3>';
            messages.forEach(function (msg) {
                html += `<div>${msg}</div>`
            });
            divMessages.innerHTML = html;
            divMessages.classList.remove("hidden");
        } else {
            divMessages.classList.add("hidden");
        }
    }

    function getAdminControls(player, arrange, kill) {
        if (arrange) {
            return `<td>
            <a href="#nowhere" class="href-btn" onclick="return routeClick(this, 'movePlayerUp');">
                <img src="/images/up.svg" class="connection-status" alt="move player up" title="move player up">
            </a>
            </td>`;
        } else if (kill) {
            if (player.status === "ALIVE") {
                return `<td>
                <button onclick="return routeClick(this, 'kill');">kill</button>
                </td>`;
            } else {
                return '<td>&nbsp;</td>';
            }
        }
        return "";
    }

    function renderRoster(state, kill) {

        let arrange = session.moderator() && state.gameStatus === "SETUP";
        let prompt = state.prompt;
        let selectedPlayers = session.selectedPlayers();

        let html = "";

        state.players.forEach(function (player) {

            let controls = getAdminControls(player, arrange, kill);

            if (state.canNominate) {
                if (session.playerName() !== player.name
                    && state.possibleNominations.indexOf(player.name) >= 0) {
                    controls += `<td>
                    <button onclick="routeClick(this, 'nominate');">nominate</button>
                    </td>`;
                } else {
                    controls += '<td>&nbsp;</td>';
                }
            } else if (prompt) {

                if (prompt.canSelectSelf || player.name !== session.playerName()) {
                    let selected = selectedPlayers.indexOf(player.name) >= 0 ? 'class="selected"' : '';
                    controls += `<td>
                    <button onclick="routeClick(this, 'select');"${selected}>Select</button>
                    </td>`;
                } else {
                    controls += '<td>&nbsp;</td>';
                }
            }

            html += `<tr>
                <td>${player.name}</td>
                <td>
                    <img src="/images/${player.connected ? "connected.svg" : "disconnected.svg"}" class="connection-status">
                </td>
                <td>${player.status}</td>
                ${controls}
                </tr>`;
        });

        rosterBody.innerHTML = html;
        roster.classList.remove("hidden");
    }

    let gameStatusUI = {
        "JOINABLE": { header: "Waiting for others to join...", bodyClass: "", modCanKill: false },
        "SETUP": { header: "Moderator is setting up.", bodyClass: "", modCanKill: false },
        "DAY_NOMINATE": { header: "Day: Accepting Nominations", bodyClass: "day", modCanKill: true },
        "DAY_VOTE": { header: "Day: Vote", bodyClass: "day", modCanKill: true },
        "NIGHT": { header: "Night", bodyClass: "night", modCanKill: true },
        "EVIL_WINS": { header: "Evil Wins", bodyClass: "evil-wins", modCanKill: false },
        "GOOD_WINS": { header: "Good Wins", bodyClass: "good-wins", modCanKill: false }
    };

    function render(state) {

        let settings = gameStatusUI[state.gameStatus];
        gameHeader.textContent = settings.header;
        document.body.className = settings.bodyClass;

        renderSetup(state);
        renderStart(state);
        renderRole(state.role);
        renderRoster(state, settings.modCanKill && session.moderator());

        if (state.canNominate
            || (state.prompt && state.prompt.dismissable)) {
            decline.classList.remove("hidden");
        } else {
            decline.classList.add("hidden");
        }

        session.prompt(state.prompt);
        if (state.prompt) {
            promptHeader.innerText = " - " + state.prompt.prompt;
            promptHeader.classList.remove("hidden");
        } else {
            promptHeader.classList.add("hidden");
        }

        renderVote(state);
        renderMessages(state.playerMessages);
    }

    function ack(msg) {

        session.playerName(msg.playerName);
        session.gameCode(msg.gameCode);
        session.moderator(msg.moderator);

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
            showErr(err);
            console.log("ws err:", err);
        };
    }

    function move(m) {
        let msg = {
            playerName: session.playerName(),
            gameCode: session.gameCode(),
            move: m
        }
        ws.send(JSON.stringify(msg));
    }

    function checkSession() {
        if (session.gameCode()) {
            connect(session.playerName(), session.gameCode());
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
                .catch(showErr)
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
                .catch(showErr)
                .finally(function () {
                    evt.target.removeAttribute("disabled");
                });
        },
        startSetup: function () {
            move({ type: "SETUP" });
        },
        start: function () {

            let orderedPlayers = [];

            for (let i = 0; i < rosterBody.childNodes.length; i++) {
                let node = firstChildElement(rosterBody.childNodes[i]);
                orderedPlayers.push(node.textContent.trim());
            }
            move({ type: "START", names: orderedPlayers });
        }
    };

    let clickHandlers = {
        movePlayerUp: function (href) {
            let tr = href.parentElement.parentElement;
            tr.parentElement.insertBefore(tr, tr.previousSibling);
        },
        kill: function (href) {

            if (!confirm("You can't un-kill someone. Are you sure?")) {
                return;
            }

            let div = href.parentElement.parentElement;
            let node = firstChildElement(div);

            move({ type: "KILL", names: [node.textContent.trim()] });
        },
        nominate: function (btn) {

            let div = btn.parentElement.parentElement;
            let node = firstChildElement(div);

            move({ type: "NOMINATE", names: [node.textContent.trim()] })
        },
        decline: function (_btn) {
            let prompt = session.prompt();
            if (prompt) {
                move({
                    type: "USE_ABILITY",
                    ability: prompt.ability,
                    names: []
                });
            } else {
                move({ type: "NOMINATE", names: [] })
            }
        },
        select: function (btn) {

            let selectedPlayers = session.selectedPlayers();
            let prompt = session.prompt();

            let div = btn.parentElement.parentElement;
            let node = firstChildElement(div);
            let playerName = node.textContent.trim();

            if (btn.classList.contains("selected")) {
                let index = selectedPlayers.indexOf(playerName);
                selectedPlayers.splice(index, 1);
                btn.classList.remove("selected");
            } else {
                btn.classList.add("selected");
                selectedPlayers.push(playerName);
            }

            if (selectedPlayers.length === prompt.count) {

                move({
                    type: "USE_ABILITY",
                    ability: prompt.ability,
                    names: selectedPlayers
                });

                // clear
                selectedPlayers = [];
                session.prompt(null);
            }

            session.selectedPlayers(selectedPlayers);
        },
        vote: function (_btn, confirmed) {
            move({ type: "VOTE", confirmed: confirmed });
        }
    };

    window.routeClick = function (elem, name, arg) {
        clickHandlers[name](elem, arg);
        return false;
    };

    // wire-up handlers
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