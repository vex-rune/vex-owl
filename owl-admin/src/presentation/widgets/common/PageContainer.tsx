import React from 'react';
import { Container, Title, Actions, Content } from './styles';

interface PageContainerProps {
  title: string;
  actions?: React.ReactNode;
  children: React.ReactNode;
}

export const PageContainer: React.FC<PageContainerProps> = ({ title, actions, children }) => {
  return (
    <>
      <Container>
        <Title>{title}</Title>
        {actions && <Actions>{actions}</Actions>}
      </Container>
      <Content>{children}</Content>
    </>
  );
};