package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final MemberRepositoryV2 memberRepository;
    private final DataSource dataSource;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection connection = dataSource.getConnection();
        try{
            connection.setAutoCommit(false); //트랜잭션 시작
            bizLogic(connection, fromId, toId, money); //비지니스 로직
            connection.commit(); //커밋
        }catch (Exception e){
            connection.rollback(); //롤백
            throw new IllegalStateException(e);
        }finally {
            release(connection);
        }
    }

    private void bizLogic(Connection connection, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(connection, fromId);
        Member toMember = memberRepository.findById(connection, toId);

        memberRepository.update(connection, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(connection, toId, toMember.getMoney() + money);
    }

    private void release(Connection connection) {
        if(connection != null){
            try{
                connection.setAutoCommit(true); //autocommit을 true로 다시 설정해줘야 한다.
                connection.close();
            }catch(Exception e){
                log.info("error", e);
            }
        }
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex"))
            throw new IllegalStateException("이체중 예외 발생");
    }
}
