package com.mygdx.catan;

import com.esotericsoftware.kryo.Kryo;
import com.mygdx.catan.account.Account;
import com.mygdx.catan.enums.*;
import com.mygdx.catan.gameboard.GameBoard;
import com.mygdx.catan.gameboard.Hex;
import com.mygdx.catan.gameboard.Knight;
import com.mygdx.catan.gameboard.Knight.Strength;
import com.mygdx.catan.gameboard.Village;
import com.mygdx.catan.player.CityImprovements;
import com.mygdx.catan.player.Player;
import com.mygdx.catan.request.*;
import com.mygdx.catan.request.game.BrowseGames;
import com.mygdx.catan.response.*;
import com.mygdx.catan.response.game.GameList;
import com.mygdx.catan.session.Session;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.objenesis.strategy.SerializingInstantiatorStrategy;

import java.util.*;

public class Config {
    /** TCP port used when connecting to the server */
    public static final int TCP = 54555;

    /** UDP port used when connecting to the server */
    public static final int UDP = 54777;

    /** Maximum number of players in a game */
    public static final int MAX_PLAYERS = 5;

    /** Minimum number of players required to start a game */
    public static final int MIN_PLAYERS = 1;    // TODO set to 3 when releasing

    /** Path of the file where the current account is stored */
    public static final String ACCOUNT_PATH = "acct.bin";

    /** The IP of the server. All clients need to connect to this IP. */
    static final String IP = "localhost";

    public static void registerKryoClasses(Kryo kryo) {
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new SerializingInstantiatorStrategy()));

        kryo.register(Account.class);
        kryo.register(Integer.class);
        kryo.register(PlayerColor.class);
        kryo.register(Player.class);
        kryo.register(CityImprovements.class);
        kryo.register(Player[].class);
        kryo.register(ImmutablePair.class);
        kryo.register(Pair.class);
        kryo.register(DiceRollPair.class);
        kryo.register(ResourceKind.class);
        kryo.register(TerrainKind.class);
        kryo.register(ResourceMap.class);
        kryo.register(ProgressCardType.class);
        kryo.register(FishTokenType.class);
        kryo.register(FishTokenMap.class);
        kryo.register(VillageKind.class);
        kryo.register(EdgeUnitKind.class);
        kryo.register(EventKind.class);
        kryo.register(GamePhase.class);
        kryo.register(com.mygdx.catan.game.Game.class);
        kryo.register(Session.class);
        
        kryo.register(GameBoard.class);
        kryo.register(HarbourKind.class);
        kryo.register(Stack.class);
        kryo.register(Hex.class);
        kryo.register(CoordinatePair.class);
        kryo.register(Village.class);
        kryo.register(Knight.class);
        kryo.register(Strength.class);
        
        kryo.register(LoginRequest.class);
        kryo.register(LoginResponse.class);
        kryo.register(BrowseGames.class);
        kryo.register(JoinRandomGame.class);
        kryo.register(CreateGame.class);
        kryo.register(StartGame.class);
        kryo.register(GameResponse.class);
        kryo.register(GameList.class);
        kryo.register(MarkAsReady.class);
        kryo.register(MarkedAsReady.class);
        kryo.register(ForwardedRequest.class);
        kryo.register(TargetedRequest.class);
        kryo.register(LinkedHashMap.class);
        kryo.register(List.class);
        kryo.register(ArrayList.class);
        kryo.register(Map.class);
        kryo.register(HashMap.class);
        kryo.register(EnumMap.class);
        kryo.register(ChooseResourceCardRequest.class);
        kryo.register(TargetedChooseResourceCardRequest.class);
        kryo.register(DistributeResources.class);
        kryo.register(GiveResources.class);
        kryo.register(TakeResources.class);
        kryo.register(TakeProgressCard.class);
        kryo.register(TargetedShowProgressCardsRequest.class);
        kryo.register(ChooseOpponentProgressCard.class);
        kryo.register(LeaveGame.class);
        kryo.register(PlayerJoined.class);
        kryo.register(PlayerLeft.class);
        kryo.register(RollDice.class);
        kryo.register(DiceRolled.class);
        kryo.register(VillagePillaged.class);
        kryo.register(PillageVillageRequest.class);
        kryo.register(DrawProgressCard.class);
        kryo.register(OpponentDrawnProgressCard.class);
        kryo.register(BuildIntersection.class);
        kryo.register(UpdateResources.class);
        kryo.register(SwitchHexDiceNumbers.class);
        kryo.register(BuildEdge.class);
        kryo.register(MoveEdge.class);
        kryo.register(EndTurn.class);
        kryo.register(TradeProposal.class);
        kryo.register(TradeOfferAccept.class);
        kryo.register(TradeOfferCancel.class);
        kryo.register(TradeCancel.class);
        kryo.register(SpecialTradeRequest.class);
        kryo.register(MoveRobberRequest.class);
        kryo.register(MovePirateRequest.class);
        kryo.register(UpdateVP.class);
        kryo.register(UpdateVillage.class);
        kryo.register(BestPlayersWin.class);
        kryo.register(DefenderOfCatan.class);
        kryo.register(DiscardHalfRequest.class);
        kryo.register(HandHalfDiscarded.class);
        kryo.register(MoveMerchantRequest.class);
        kryo.register(CityWallChange.class);
        kryo.register(UpdateLongestRoad.class);
        kryo.register(DisplaceRoadRequest.class);
        kryo.register(UpdateOldBoot.class);
        kryo.register(TradeImprovementRequest.class);
        kryo.register(ScienceImprovementRequest.class);
        kryo.register(PoliticsImprovementRequest.class);
        kryo.register(KnightRequest.class);
        kryo.register(KnightRequest.Type.class);
        kryo.register(MoveKnightRequest.class);
        kryo.register(ActivateKnightRequest.class);
        kryo.register(ChangeKnightStatus.class);
        kryo.register(BarbarianPositionChange.class);
    }
}
